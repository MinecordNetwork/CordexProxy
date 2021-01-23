package net.minecord.cordexproxy.listener

import net.minecord.cordexproxy.CordexProxy
import net.md_5.bungee.api.event.ServerConnectEvent
import net.md_5.bungee.api.event.ServerConnectedEvent
import net.md_5.bungee.event.EventHandler

import java.util.HashMap

class PlayerListener(cordexProxy: CordexProxy) : BaseListener(cordexProxy) {
    @EventHandler
    fun onServerConnect(e: ServerConnectedEvent) {
        val cordPlayer = cordexProxy.playerController.getPlayer(e.player)

        cordexProxy.playerController.showTablist(cordPlayer)

        if (cordPlayer.data.connectTime!!.time < System.currentTimeMillis() - 2000) {
            val server = cordexProxy.serverController.getServer(e.server.info.name)

            val placeholders = HashMap<String, String>()
            placeholders["%server%"] = server.displayName
            cordPlayer.sendMessage("system", cordPlayer.translateMessage("connectedToServer"), placeholders)
        }
    }

    @EventHandler
    fun onServerChange(e: ServerConnectEvent) {
        if (e.reason == ServerConnectEvent.Reason.JOIN_PROXY) {
            return
        }

        val cordPlayer = cordexProxy.playerController.getPlayer(e.player)

        if (!cordexProxy.databaseController.isLogged(cordPlayer.data.id)) {
            e.isCancelled = true
        } else {
            val server = cordexProxy.serverController.getServer(e.target.name)

            val placeholders = HashMap<String, String>()
            placeholders["%server%"] = server.displayName
            cordPlayer.sendMessage("system", cordPlayer.translateMessage("connectingToServer"), placeholders)
        }
    }
}
