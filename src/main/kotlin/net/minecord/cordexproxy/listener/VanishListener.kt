package net.minecord.cordexproxy.listener

import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.event.ChatEvent
import net.md_5.bungee.api.event.ServerConnectedEvent
import net.md_5.bungee.event.EventHandler
import net.minecord.cordexproxy.CordexProxy
import java.lang.NullPointerException

class VanishListener(cordexProxy: CordexProxy) : BaseListener(cordexProxy) {
    @EventHandler
    fun onChat(e: ChatEvent) {
        val command = e.message

        if (command != "/v" && command != "/vanish")
            return

        val cordPlayer = cordexProxy.playerController.getPlayer(e.sender as ProxiedPlayer)
        if (cordPlayer.rank.isAdmin) {
            cordPlayer.onVanish()
        }
    }

    @EventHandler
    fun onServerChange(e: ServerConnectedEvent) {
        try {
            val cordPlayer = cordexProxy.playerController.getPlayerByUniqueId(e.player.uniqueId)

            if (cordPlayer.rank.isAdmin) {
                cordPlayer.show()
            }
        } catch (e: NullPointerException) {}
    }
}
