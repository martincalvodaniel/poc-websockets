package com.dmartinc

import com.fasterxml.jackson.databind.ObjectMapper
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import javax.enterprise.context.ApplicationScoped
import javax.websocket.*
import javax.websocket.server.ServerEndpoint

@ApplicationScoped
@ServerEndpoint("/websocket")
class StartWebSocket(private val objectMapper: ObjectMapper) {

    val rooms = ConcurrentHashMap<String, MutableList<Session>>()

    @OnOpen
    fun onOpen(session: Session?) {
        println("onOpen> OK ${session?.id}")
    }

    @OnClose
    fun onClose(session: Session?) {
        println("onClose> OK")
    }

    @OnError
    fun onError(session: Session?, throwable: Throwable) {
        println("onError> $throwable")
    }

    @OnMessage
    fun onMessage(session: Session?, message: ByteBuffer) {
        val messageJson = String(message.array(), Charset.defaultCharset())
        println("onMessageByteBuffer> $messageJson")
        val messageObject = objectMapper.readValue(messageJson, Message::class.java)
        if (messageObject.isChannelRoom() && messageObject.isTypeCreate()) session?.let {
            val roomId = UUID.randomUUID().toString().substring(0, 8)
            rooms.computeIfAbsent(roomId) { mutableListOf() }
            rooms[roomId]?.add(it)
            val roomCreatedResponse = RoomCreatedResponse.from(messageObject, roomId)
            it.asyncRemote.sendText(objectMapper.writeValueAsString(roomCreatedResponse))
        }
        rooms[messageObject.payload.roomId]
            ?.map { it.asyncRemote.sendText(messageJson) }
            ?.forEach { it.get() }
    }

    data class Message(val channel: String, val type: String, val payload: Payload) {
        data class Payload(val userId: String, val userName: String, val roomId: String? = null, val userMessage: String? = null)
        fun isChannelRoom() = channel == "ROOM"
        fun isTypeCreate() = type == "create"
    }

    data class RoomCreatedResponse(val channel: String, val type: String, val payload: Payload, val message: String) {
        companion object {
            fun from(message: Message, roomId: String) = RoomCreatedResponse(message.channel, "created", Payload.from(message.payload, roomId), "Created room $roomId")
        }
        data class Payload(val userId: String, val userName: String, val characterId: Int, val roomId: String? = null, val userMessage: String? = null) {
            companion object {
                fun from(payload: Message.Payload, roomId: String) = Payload(payload.userId, payload.userName, 0, roomId, payload.userMessage)
            }
        }
    }
}
