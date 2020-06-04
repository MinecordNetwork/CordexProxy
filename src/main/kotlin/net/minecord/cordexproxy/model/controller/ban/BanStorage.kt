package net.minecord.cordexproxy.model.controller.ban

import java.sql.Timestamp
import java.util.concurrent.TimeUnit


class BanStorage(var id: Int, var targetId: Int, var targetNick: String, var targetIp: Int, var adminId: Int, var adminIp: Int, var reason: String, var expire: Timestamp, var isIpBan: Boolean, var isActive: Boolean) {
    fun getFriendlyLeftTime(): String {
        val seconds = (expire.time - System.currentTimeMillis()) / 1000

        val days = TimeUnit.SECONDS.toDays(seconds).toInt()
        val hours = TimeUnit.SECONDS.toHours(seconds) - days * 24
        val minutes = (TimeUnit.SECONDS.toMinutes(seconds) - TimeUnit.SECONDS.toHours(seconds) * 60) + 1

        return if (days == 0) {
            return if (hours == 0L) {
                "$minutes min"
            } else {
                "${hours}h ${minutes}m"
            }
        } else {
            "${days}d ${hours}h ${minutes}m"
        }
    }
}
