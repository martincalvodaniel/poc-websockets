package com.dmartinc

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

//@ServerEndpoint("/{roomId}")
@ServerEndpoint("/websocket")
@ApplicationScoped
class WebSocket(private val objectMapper: ObjectMapper) {

    val rooms = ConcurrentHashMap<String, MutableList<Session>>()

    @OnOpen
    fun onOpen(session: Session?) {
        println("onOpen> [${session?.id}]")
    }

    @OnClose
    fun onClose(session: Session?) {
        println("onClose> [${session?.id}]")
    }

    @OnError
    fun onError(session: Session?, throwable: Throwable) {
        println("onError> [${session?.id}] $throwable")
    }

//    @OnMessage
//    fun onMessage(session: Session?, message: Message) {
//        println("$message")
//    }

    @OnMessage
    fun onMessage(/*@PathParam("roomId") roomId: String, */session: Session?, message: String) {
        println("onMessage(String)> [${session?.id}] $message")
        val messageObject = objectMapper.readValue(message, Message::class.java)
        if (messageObject.isChannelRoom() && messageObject.isTypeCreate()) {
            session?.let {
                val roomId = UUID.randomUUID().toString().substring(0, 8)
                rooms.computeIfAbsent(roomId) { mutableListOf() }
                rooms[roomId]?.add(it)
                val roomCreatedResponse = messageObject.copy(
                    type = "created",
                    user = messageObject.user?.copy(characterId = 0),
                    roomId = roomId,
                    payload = messageObject.payload?.copy(userMessage = "Created room $roomId")
                )
                it.asyncRemote.sendText(objectMapper.writeValueAsString(roomCreatedResponse))
            }
        }
        rooms[messageObject.roomId]
            ?.map { it.asyncRemote.sendText(message) }
            ?.forEach { it.get() }
    }

    @OnMessage
    fun onMessage(session: Session?, message: ByteBuffer) {
        onMessage(session, String(message.array(), Charset.defaultCharset()))
    }

    data class Message(
        val channel: String? = null,
        val type: String? = null,
        val user: User? = null,
        val roomId: String? = null,
        val payload: Payload? = null,
    ) {
        data class User(
            val id: String? = null,
            val name: String? = null,
            val characterId: Int? = null,
            val userMessage: String? = null,
        )
        data class Payload(
            val userMessage: String? = null,
        )

        fun isChannelRoom() = channel == "ROOM"
        fun isTypeCreate() = type == "create"
    }
}
