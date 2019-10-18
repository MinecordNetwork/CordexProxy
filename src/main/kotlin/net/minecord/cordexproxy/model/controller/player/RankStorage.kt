package net.minecord.cordexproxy.model.controller.player

import net.md_5.bungee.api.ChatColor

class RankStorage(private val name: String, val basicPermission: String, private val stringColor: String, val chatColor: ChatColor) {
    val isAdmin: Boolean
        get() {
            return when (basicPermission) {
                "owner.global" -> true
                "admin.global" -> true
                "builder.global" -> true
                "support.global" -> true
                "mapmaker.global" -> true
                "trainee.global" -> true
                else -> false
            }
        }

    val prefix: String
        get() = ChatColor.translateAlternateColorCodes('&', stringColor + name.toUpperCase()) + ChatColor.RESET + " "

    fun getName(): String {
        return name
    }
}
