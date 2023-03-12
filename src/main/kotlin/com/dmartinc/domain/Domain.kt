package com.dmartinc.domain

interface WebSocketAction

data class CreateRoomAction(val user: User) : WebSocketAction
data class PlayerHasCreatedRoomEvent(val player: Player, val room: Room)

data class JoinRoomAction(val user: User, val roomId: String) : WebSocketAction
data class PlayerHasJoinedRoomEvent(val player: Player, val room: Room)
data class PlayerHasLeavedRoomEvent(val userId: String, val roomId: String)

data class RoomMessage(val userId: String, val roomId: String, val message: String)

data class Room(val id: String, val userIds: List<String>)
data class Player(val user: User, val characterId: Int)
data class User(val id: String, val name: String) {
    fun toPlayer(characterId: Int) = Player(this, characterId)
}