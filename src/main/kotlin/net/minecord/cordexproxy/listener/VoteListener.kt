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
        //val placeholders = HashMap<String, String>()

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

        //placeholders["%prefix%"] = cordexProxy.chatController.getPrefix("vote")
        //placeholders["%service%"] = service

        val playerId = if (player != null) {
            val cordPlayer = cordexProxy.playerController.getPlayer(player)
            cordPlayer.data.id
        } else {
            val data = cordexProxy.databaseController.loadPlayerData(vote.username)
            data?.id ?: return
        }

        /*if (player != null) {
            val cordPlayer = cordexProxy.playerController.getPlayer(player)
            //placeholders["%player%"] = cordPlayer.player.name + ""
            //placeholders["%rcolor%"] = cordPlayer.rank.stringColor + ""
            cordexProxy.databaseController.insertVote(cordPlayer.data.id, serverListId)
        } else {
            //placeholders["%player%"] = vote.username
            //placeholders["%rcolor%"] = "&#447eff"
            val data = cordexProxy.databaseController.loadPlayerData(vote.username)
            if (data != null) {
                cordexProxy.databaseController.insertVote(data.id, serverListId)
            }
        }*/

        cordexProxy.databaseController.insertVote(playerId, serverListId)
        cordexProxy.databaseController.insertDelivery(playerId, 16, 1, "survival")
        cordexProxy.databaseController.insertDelivery(playerId, 399, 1, "survival")

        //cordexProxy.translationController.broadcastTranslate("playerVoted", placeholders)
    }
}
