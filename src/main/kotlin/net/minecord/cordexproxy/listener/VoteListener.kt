package net.minecord.cordexproxy.listener

import com.vexsoftware.votifier.bungee.events.VotifierEvent
import net.md_5.bungee.api.ProxyServer
import net.minecord.cordexproxy.CordexProxy
import net.md_5.bungee.event.EventHandler

class VoteListener(cordexProxy: CordexProxy) : BaseListener(cordexProxy) {
    @EventHandler
    fun onVote(e: VotifierEvent) {
        val vote = e.vote
        val player = ProxyServer.getInstance().getPlayer(vote.username)

        var service = vote.serviceName

        if (service.contains("Minecraft-MP")) {
            service = "Minecraft-MP"
        }

        if (service.contains("PlanetMinecraft")) {
            service = "PlanetMinecraft"
        }

        if (service.contains("MinecraftServery")) {
            service = "MinecraftServery.eu"
        }

        val serverListId = cordexProxy.databaseController.getServerlistId(service)

        val playerId = if (player != null) {
            val cordPlayer = cordexProxy.playerController.getPlayer(player)
            cordPlayer.data.id
        } else {
            val data = cordexProxy.databaseController.loadPlayerData(vote.username)
            data?.id ?: return
        }

        cordexProxy.databaseController.insertVote(playerId, serverListId)
        cordexProxy.databaseController.insertDelivery(playerId, 16, 1, "survival")
        cordexProxy.databaseController.insertDelivery(playerId, 399, 1, "survival")
    }
}
