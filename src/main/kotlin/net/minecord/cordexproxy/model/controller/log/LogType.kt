package net.minecord.cordexproxy.model.controller.log

import net.md_5.bungee.api.ChatColor

enum class LogType private constructor(val chatColor: ChatColor) {
    ERROR(ChatColor.RED), DEBUG(ChatColor.YELLOW), INFO(ChatColor.AQUA), WARNING(ChatColor.GOLD), COLLECTOR(ChatColor.GREEN)
}
