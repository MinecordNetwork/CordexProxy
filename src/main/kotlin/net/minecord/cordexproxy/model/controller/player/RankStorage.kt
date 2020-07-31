package net.minecord.cordexproxy.model.controller.player

import net.md_5.bungee.api.ChatColor
import net.minecord.cordexproxy.util.colored

class RankStorage(val name: String, val basicPermission: String, val stringColor: String, val chatColor: ChatColor) {
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
        get() = (stringColor + "&l" + name.toUpperCase()).colored() + ChatColor.RESET + " "
}
