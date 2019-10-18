package net.minecord.cordexproxy.model.controller.player

import java.sql.Timestamp
import java.util.UUID

class PlayerStorage(val id: Int, val name: String, val uuid: UUID, val isOnline: Boolean, var isLogged: Boolean, val type: String, val isWhitelisted: Boolean, val firstIp: Int, val firstIpAddress: String, val lastIp: Int, var lastIpAddress: String, playedTime: Int, var firstJoin: Timestamp?, var firstLogin: Timestamp?, var lastJoin: Timestamp?, var lastLogin: Timestamp?, val rank: RankStorage) {
    var playedTime: Int = 0
        get() = if (isOnline) (System.currentTimeMillis() - connectTime!!.time).toInt() / 1000 + field else field
    var connectTime: Timestamp? = null

    private val onlineTime: Int
        get() = if (isOnline) (System.currentTimeMillis() - connectTime!!.time).toInt() / 1000 else 0

    init {
        this.playedTime = playedTime
        this.connectTime = Timestamp(System.currentTimeMillis())
    }

    fun getFormattedPlayedTime(showSeconds: Boolean): String {
        return formatTime(playedTime, showSeconds)
    }

    fun getFormattedOnlineTime(showSeconds: Boolean): String {
        return formatTime(onlineTime, showSeconds)
    }

    private fun formatTime(onlineTime: Int, showSeconds: Boolean): String {
        val hours = onlineTime / 3600
        val minutes = onlineTime % 3600 / 60

        return if (showSeconds) String.format("%02d:%02d:%02d", hours, minutes, onlineTime % 60) else String.format("%02d:%02d", hours, minutes)
    }
}
