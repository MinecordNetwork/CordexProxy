package net.minecord.cordexproxy.listener

import com.vexsoftware.votifier.bungee.events.VotifierEvent
import net.md_5.bungee.api.ProxyServer
import net.minecord.cordexproxy.CordexProxy
import net.md_5.bungee.event.EventHandler

import java.util.HashMap

class VoteListener(cordexProxy: CordexProxy) : BaseListener(cordexProxy) {
    @EventHandler
    fun onVote(e: VotifierEvent) {
        val vote = e.vote
        val player = ProxyServer.getInstance().getPlayer(vote.username)
        val placeholders = HashMap<String, String>()

        var service = vote.serviceName

        if (service.contains("Minecraft-MP")) {
            service = "MinecraftMP"
        }

        if (service.contains("PlanetMinecraft")) {
            service = "PlanetMinecraft"
        }

        placeholders["%prefix%"] = cordexProxy.chatController.getPrefix("vote")
        placeholders["%service%"] = service
        if (player != null) {
            val cordPlayer = cordexProxy.playerController.getPlayer(player)
            placeholders["%player%"] = cordPlayer.player.name + ""
            placeholders["%rcolor%"] = cordPlayer.rank.stringColor + ""
            cordexProxy.databaseController.insertVote(cordPlayer.data.id, service)
        } else {
            placeholders["%player%"] = vote.username
            placeholders["%rcolor%"] = "&9"
            val data = cordexProxy.databaseController.loadPlayerData(vote.username)
            if (data != null) {
                cordexProxy.databaseController.insertVote(data.id, service)
            }
        }

        cordexProxy.translationController.broadcastTranslate("playerVoted", placeholders)
    }
}
