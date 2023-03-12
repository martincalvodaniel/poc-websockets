package com.dmartinc.websockets

import com.dmartinc.domain.CreateRoomAction
import com.dmartinc.domain.JoinRoomAction
import com.dmartinc.domain.PlayerHasCreatedRoomEvent
import com.dmartinc.domain.PlayerHasJoinedRoomEvent
import com.dmartinc.domain.PlayerHasLeavedRoomEvent
import com.dmartinc.domain.Room
import com.dmartinc.domain.RoomMessage
import com.dmartinc.domain.WsException
import com.dmartinc.websockets.WsMessage.Companion.fromDomain
import com.fasterxml.jackson.databind.ObjectMapper
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.util.UUID
import java.util.concurrent.ThreadLocalRandom
import javax.enterprise.context.ApplicationScoped
import javax.websocket.OnClose
import javax.websocket.OnError
import javax.websocket.OnMessage
import javax.websocket.OnOpen
import javax.websocket.Session
import javax.websocket.server.ServerEndpoint

@ServerEndpoint("/")
@ApplicationScoped
class WebSocket(private val objectMapper: ObjectMapper, private val sessionsManager: SessionsManager) {

    @OnOpen
    fun onOpen(session: Session) {
        println("onOpen> [${session.id}]")
        sessionsManager.save(session)
    }

    @OnClose
    fun onClose(session: Session) {
        println("onClose> [${session.id}]")
        leaveRoom(session)
    }

    @OnError
    fun onError(session: Session, throwable: Throwable) {
        val errorMessage = if (throwable is WsException) throwable.message else {
            throwable.printStackTrace()
            "Internal error"
        }
        println("onError> [${session.id}] $errorMessage")
        val wsErrorMessage = WsErrorMessage(errorMessage)
        val wsErrorMessageJson = objectMapper.writeValueAsString(wsErrorMessage)
        session.asyncRemote.sendText(wsErrorMessageJson)
    }

    @OnMessage
    fun onMessage(session: Session, message: ByteBuffer) {
        onMessage(session, String(message.array(), Charset.defaultCharset()))
    }

    @OnMessage
    fun onMessage(session: Session, message: String) {
        println("onMessage(String)> [${session.id}] $message")
        val rqWsMessage = objectMapper.readValue(message, WsMessage::class.java)
        val type = rqWsMessage.type ?: throw WsException("No type")
        when (type) {
            "createRoom" -> {
                createRoom(session.id, rqWsMessage.createRoomAction())
            }

            "joinRoom" -> {
                joinRoom(session.id, rqWsMessage.joinRoomAction())
            }

            "roomMessage" -> {
                roomMessage(rqWsMessage.roomMessage())
            }

            "roomInfo" -> {
                roomInfo(session.id, rqWsMessage.roomId())
            }

            else -> {
                TODO()
            }
        }
    }

    fun createRoom(sessionId: String, createRoomAction: CreateRoomAction) {
        val roomId = UUID.randomUUID().toString().substring(0, 8)
        val characterId = characterId()

        sessionsManager.createRoom(sessionId, roomId, createRoomAction.user.id)

        val playerHasCreatedRoomEvent = PlayerHasCreatedRoomEvent(
            player = createRoomAction.user.toPlayer(characterId),
            room = Room(roomId, listOf(createRoomAction.user.id))
        )

        sessionsManager.sendRoomMessage(playerHasCreatedRoomEvent.room.id, playerHasCreatedRoomEvent.fromDomain())
    }

    fun joinRoom(sessionId: String, joinRoomAction: JoinRoomAction) {
        val userId = joinRoomAction.user.id
        val roomId = joinRoomAction.roomId
        val characterId = characterId()

        val roomUserIds = sessionsManager.userIds(roomId)
        if (roomUserIds.contains(userId)) throw WsException("User $userId already present in room $roomId")
        sessionsManager.joinRoom(sessionId, roomId, joinRoomAction.user.id)

        val playerHasJoinedRoomEvent = PlayerHasJoinedRoomEvent(
            player = joinRoomAction.user.toPlayer(characterId),
            room = Room(roomId, sessionsManager.userIds(roomId))
        )
        println("$playerHasJoinedRoomEvent")

        sessionsManager.sendRoomMessage(playerHasJoinedRoomEvent.room.id, playerHasJoinedRoomEvent.fromDomain())
    }

    private fun characterId() = ThreadLocalRandom.current().nextInt()

    fun leaveRoom(session: Session) {
        sessionsManager.remove(session)

        val userId = sessionsManager.userId(session.id) ?: return
        val roomId = sessionsManager.roomId(session.id) ?: return

        sessionsManager.leaveRoom(session.id, roomId, userId)
        val playerHasJoinedRoomEvent = PlayerHasLeavedRoomEvent(userId, roomId)

        sessionsManager.sendRoomMessage(roomId, playerHasJoinedRoomEvent.fromDomain())
    }

    fun roomMessage(roomMessage: RoomMessage) {
        sessionsManager.sendRoomMessage(roomMessage.roomId, roomMessage)
    }

    fun roomInfo(sessionId: String, roomId: String) {
        sessionsManager.sendMessage(sessionId, Room(roomId, sessionsManager.userIds(roomId)))
    }

    data class WsErrorMessage(val errorMessage: String)
}
