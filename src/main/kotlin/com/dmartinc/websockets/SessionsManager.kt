package com.dmartinc.websockets

import com.dmartinc.domain.WsException
import com.fasterxml.jackson.databind.ObjectMapper
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import javax.enterprise.context.ApplicationScoped
import javax.websocket.Session

@ApplicationScoped
class SessionsManager(private val objectMapper: ObjectMapper) {

    private val sessionsById = ConcurrentHashMap<String, Session>()
    private val roomIdBySessionId = ConcurrentHashMap<String, String>()
    private val sessionIdByUserId = ConcurrentHashMap<String, String>()
    private val userIdBySessionId = ConcurrentHashMap<String, String>()
    private val userIdsByRoomId = ConcurrentHashMap<String, CopyOnWriteArrayList<String>>()

    fun userIds(roomId: String): List<String> = retrieveRoomUserIds(roomId)
    fun userId(sessionId: String): String? = userIdBySessionId[sessionId]
    fun roomId(sessionId: String): String? = roomIdBySessionId[sessionId]

    fun save(session: Session) {
        sessionsById[session.id] = session
        printActiveSessions()
    }

    fun remove(session: Session) {
        sessionsById.remove(session.id)
        printActiveSessions()
    }

    fun createRoom(sessionId: String, roomId: String, userId: String) {
        userIdBySessionId[sessionId] = userId
        roomIdBySessionId[sessionId] = roomId
        sessionIdByUserId[userId] = sessionId
        userIdsByRoomId[roomId] = CopyOnWriteArrayList(listOf(userId))
    }

    fun joinRoom(sessionId: String, roomId: String, userId: String) {
        userIdBySessionId[sessionId] = userId
        roomIdBySessionId[sessionId] = roomId
        sessionIdByUserId[userId] = sessionId
        retrieveRoomUserIds(roomId).add(userId)
    }

    fun leaveRoom(sessionId: String, roomId: String, userId: String) {
        userIdBySessionId.remove(sessionId)
        roomIdBySessionId.remove(sessionId)
        sessionIdByUserId.remove(userId)
        safeRetrieveRoomUserIds(roomId)?.remove(userId)
    }

    fun sendRoomMessage(roomId: String, any: Any) {
        val userIds = userIdsByRoomId[roomId] ?: throw WsException("No room $roomId")

        userIds
            .map { sessionIdByUserId[it] }
            .mapNotNull { sessionsById[it] }
            .map { it.asyncRemote.sendText(objectMapper.writeValueAsString(any)) }
            .forEach { it.get() }
    }

    fun sendMessage(sessionId: String, any: Any) {
        (sessionsById[sessionId] ?: return).asyncRemote.sendText(objectMapper.writeValueAsString(any))
    }

    private fun retrieveRoomUserIds(roomId: String): CopyOnWriteArrayList<String> =
        userIdsByRoomId[roomId] ?: throw WsException("No room $roomId")

    private fun safeRetrieveRoomUserIds(roomId: String): CopyOnWriteArrayList<String>? = userIdsByRoomId[roomId]

    private fun printActiveSessions() {
        println("Active sessions: ${sessionsById.size}")
    }
}
