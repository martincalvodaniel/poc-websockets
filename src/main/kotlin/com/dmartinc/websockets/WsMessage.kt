package com.dmartinc.websockets

import com.dmartinc.domain.CreateRoomAction
import com.dmartinc.domain.JoinRoomAction
import com.dmartinc.domain.PlayerHasCreatedRoomEvent
import com.dmartinc.domain.PlayerHasJoinedRoomEvent
import com.dmartinc.domain.PlayerHasLeavedRoomEvent
import com.dmartinc.domain.RoomMessage
import com.dmartinc.domain.User
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class WsMessage(
    val type: String? = null,
    val user: WsUser? = null,
    val room: WsRoom? = null,
    val payload: WsPayload? = null,
) {
    companion object {
        fun PlayerHasCreatedRoomEvent.fromDomain() = WsMessage(
            javaClass.simpleName,
            WsUser(player.user.id, player.user.name, player.characterId),
            WsRoom(room.id, room.userIds),
        )

        fun PlayerHasJoinedRoomEvent.fromDomain() = WsMessage(
            javaClass.simpleName,
            WsUser(player.user.id, player.user.name, player.characterId),
            WsRoom(room.id, room.userIds),
        )

        fun PlayerHasLeavedRoomEvent.fromDomain() = WsMessage(
            javaClass.simpleName,
            WsUser(userId),
            WsRoom(roomId),
        )
    }

    fun createRoomAction() = CreateRoomAction(toUser())
    fun joinRoomAction() = JoinRoomAction(toUser(), room!!.id!!)
    fun roomMessage() = RoomMessage(user!!.id!!, room!!.id!!, payload!!.message!!)
    fun roomId() = room!!.id!!
    private fun toUser() = User(user!!.id!!, user.name!!)

    @JsonInclude(JsonInclude.Include.NON_NULL)
    data class WsUser(
        val id: String? = null,
        val name: String? = null,
        val characterId: Int? = null,
    )

    @JsonInclude(JsonInclude.Include.NON_NULL)
    data class WsRoom(
        val id: String? = null,
        val userIds: List<String>? = null,
    )

    @JsonInclude(JsonInclude.Include.NON_NULL)
    data class WsPayload(
        val message: String? = null,
    )
}
