package net.minecord.cordexproxy.controller

import net.minecord.cordexproxy.CordexProxy
import net.minecord.cordexproxy.model.controller.log.LogType
import net.md_5.bungee.api.ChatColor

class LogController(cordexProxy: CordexProxy) : BaseController(cordexProxy) {
    fun log(message: String, logType: LogType) {
        cordexProxy.logger.info(ChatColor.translateAlternateColorCodes('&', logType.chatColor.toString() + "[" + logType.toString() + "] &7" + message))
    }

    fun write(message: String) {
        cordexProxy.logger.info(ChatColor.translateAlternateColorCodes('&', message))
    }
}
