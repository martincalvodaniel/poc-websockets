package com.dmartinc

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import javax.enterprise.context.ApplicationScoped
import javax.websocket.OnClose
import javax.websocket.OnError
import javax.websocket.OnMessage
import javax.websocket.OnOpen
import javax.websocket.Session
import javax.websocket.server.ServerEndpoint

@ServerEndpoint("/")
@ApplicationScoped
class WebSocket(private val objectMapper: ObjectMapper) {

    val sessionsById = ConcurrentHashMap<String, Session>()
    val rooms = ConcurrentHashMap<String, MutableList<Session>>()

    @OnOpen
    fun onOpen(session: Session) {
        println("onOpen> [${session.id}]")
        sessionsById[session.id] = session
    }

    @OnClose
    fun onClose(session: Session) {
        println("onClose> [${session.id}]")
        sessionsById.remove(session.id)
    }

    @OnError
    fun onError(session: Session, throwable: Throwable) {
        println("onError> [${session.id}] $throwable")
        error(throwable)
    }

    @OnMessage
    fun onMessage(session: Session, message: ByteBuffer) {
        onMessage(session, String(message.array(), Charset.defaultCharset()))
    }

    @OnMessage
    fun onMessage(session: Session, message: String) {
        fun <T> mutableListOf(firstElement: T): MutableList<T> {
            val mutableList = mutableListOf<T>()
            mutableList.add(firstElement)
            return mutableList
        }

        println("onMessage(String)> [${session.id}] $message")
        val rqMessage = objectMapper.readValue(message, Message::class.java)
        val type = rqMessage.type ?: throw RuntimeException("No type")
        when (type) {
            "createRoom" -> {
                val roomId = UUID.randomUUID().toString().substring(0, 8)
                rooms[roomId] = mutableListOf(session)
                val rsMessage = rqMessage.copy(
                    type = "roomCreated",
                    room = Message.Room(roomId),
                    user = rqMessage.user?.copy(characterId = 0),
                    payload = Message.Payload(message = "${rqMessage.user?.id} created room $roomId")
                )
                session.asyncRemote.sendText(objectMapper.writeValueAsString(rsMessage))
            }
            "joinRoom" -> {
                val roomId = rqMessage?.room?.id
                val room = rooms[roomId] ?: throw RuntimeException("No room $roomId")
                val characterId = room.size
                room.add(session)
                val rsMessage = rqMessage.copy(
                    type = "roomJoined",
                    user = rqMessage.user?.copy(characterId = characterId),
                    payload = Message.Payload(message = "${rqMessage.user?.id} joined room $roomId")
                )
                val rsMessageJson = objectMapper.writeValueAsString(rsMessage)
                rooms[roomId]?.map { it.asyncRemote.sendText(rsMessageJson) }?.forEach { it.get() }
            }
            "roomMessage" -> {
                val roomId = rqMessage?.room?.id
                rooms[roomId]?.map { it.asyncRemote.sendText(message) }?.forEach { it.get() }
            }
            else -> { TODO() }
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    data class Message(
        val type: String? = null,
        val user: User? = null,
        val room: Room? = null,
        val payload: Payload? = null,
    ) {
        @JsonInclude(JsonInclude.Include.NON_NULL)
        data class User(
            val id: String? = null,
            val characterId: Int? = null,
        )
        @JsonInclude(JsonInclude.Include.NON_NULL)
        data class Room(
            val id: String? = null,
        )
        @JsonInclude(JsonInclude.Include.NON_NULL)
        data class Payload(
            val message: String? = null,
        )
    }
}
