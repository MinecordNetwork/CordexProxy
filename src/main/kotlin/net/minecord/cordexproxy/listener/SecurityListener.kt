package net.minecord.cordexproxy.listener

import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.event.ChatEvent
import net.md_5.bungee.event.EventHandler
import net.minecord.cordexproxy.CordexProxy
import net.minecord.cordexproxy.util.colored

class SecurityListener(cordexProxy: CordexProxy) : BaseListener(cordexProxy) {
    @EventHandler
    fun onChat(e: ChatEvent) {
        val sender = e.sender
        if (sender !is ProxiedPlayer) {
            return
        }

        val command = e.message
        if (command.startsWith("/l ") || command.startsWith("/login ") || command.startsWith("/reg ") || command.startsWith("/register ")) {
            return
        }

        val cordPlayer = cordexProxy.playerController.getPlayer(sender)
        if (!command.startsWith("/") || command.startsWith("/afk ")) {
            val isMuted = cordexProxy.banController.isMuted(cordPlayer.player.uniqueId)
            if (isMuted) {
                cordPlayer.sendMessage("banlist", cordPlayer.translateMessage("muteChatTry"))
                e.isCancelled = true
                return
            } else {
                /*val rightNow = Calendar.getInstance()
                val hour = rightNow[Calendar.HOUR_OF_DAY]
                val minute = rightNow[Calendar.MINUTE]*/
                cordPlayer.lastMessages.add(ChatColor.stripColor(command.colored()))
                if (cordPlayer.lastMessages.size > 10) {
                    cordPlayer.lastMessages.removeAt(0)
                }
            }
        }

        var isLogged = cordPlayer.data.isLogged
        if (!isLogged) {
            isLogged = cordexProxy.databaseController.isLogged(cordPlayer.data.id)
            cordPlayer.data.isLogged = isLogged
        }

        if (!isLogged) {
            cordPlayer.sendMessage("auth", cordPlayer.translateMessage("loginCommandDenied"))
            e.isCancelled = true
        }
    }
}
