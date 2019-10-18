package net.minecord.cordexproxy.listener

import net.minecord.bungateway.event.InsertPlayerInfoEvent
import net.minecord.bungateway.event.PlayerLogoutEvent
import net.minecord.bungateway.event.RequestPlayerInfoEvent
import net.minecord.bungateway.event.UpdatePlayerInfoEvent
import net.minecord.bungateway.model.player.PlayerInfo
import net.minecord.bungateway.model.player.PlayerType
import net.minecord.cordexproxy.CordexProxy
import net.minecord.cordexproxy.model.controller.player.PlayerStorage
import net.md_5.bungee.event.EventHandler

class GatewayListener(cordexProxy: CordexProxy) : BaseListener(cordexProxy) {
    @EventHandler
    fun requestPlayerInfo(e: RequestPlayerInfoEvent) {
        val playerStorage: PlayerStorage? = if (e.uuid == null)
            cordexProxy.cacheController.getPlayerData(e.name)
        else
            cordexProxy.cacheController.getPlayerData(e.uuid)

        if (playerStorage != null)
            e.playerInfo = PlayerInfo(playerStorage.id, playerStorage.name, playerStorage.uuid, true, PlayerType.valueOf(playerStorage.type.toUpperCase()), playerStorage.lastIpAddress, playerStorage.lastJoin, playerStorage.firstJoin)

        e.isAccepted = true
    }

    @EventHandler
    fun insertPlayerInfo(e: InsertPlayerInfoEvent) {
        val playerInfo = e.playerInfo
        val playerStorage = PlayerStorage(0, playerInfo.name, playerInfo.uuid, playerInfo.isOnline, false, playerInfo.type.toString().toLowerCase(), false, 0, playerInfo.ip, 0, playerInfo.ip, 0, null, null, null, null, cordexProxy.rankController.getRank("default"))

        cordexProxy.databaseController.insertPlayerData(playerStorage)

        e.isAccepted = true
    }

    @EventHandler
    fun updatePlayerInfo(e: UpdatePlayerInfoEvent) {
        val playerInfo = e.playerInfo
        val playerStorage = PlayerStorage(0, playerInfo.name, playerInfo.uuid, playerInfo.isOnline, false, playerInfo.type.toString().toLowerCase(), false, 0, null, 0, playerInfo.ip, 0, null, null, null, null, cordexProxy.rankController.getRank("default"))

        cordexProxy.databaseController.updatePlayerData(playerStorage)

        e.isAccepted = true
    }

    @EventHandler
    fun logoutPlayerEvent(e: PlayerLogoutEvent) {
        cordexProxy.databaseController.onQuit(e.playerInfo.uuid)

        e.isAccepted = true
    }
}
