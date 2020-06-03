package net.minecord.cordexproxy.listener

import net.md_5.bungee.api.ProxyServer
import net.minecord.cordexproxy.CordexProxy
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.event.LoginEvent
import net.md_5.bungee.api.event.PlayerDisconnectEvent
import net.md_5.bungee.api.event.PostLoginEvent
import net.md_5.bungee.event.EventHandler
import net.md_5.bungee.event.EventPriority
import net.minecord.cordexproxy.model.controller.log.LogType
import net.minecord.cordexproxy.util.colored
import java.net.Inet4Address

class ConnectionListener(cordexProxy: CordexProxy) : BaseListener(cordexProxy) {
    @EventHandler(priority = EventPriority.LOWEST)
    fun loginEvent(e: PostLoginEvent) {
        cordexProxy.playerController.addPlayer(e.player)

        if (cordexProxy.playerController.getPlayers().size > cordexProxy.cacheController.getPlayerRecord())
            cordexProxy.cacheController.setPlayerRecord(cordexProxy.playerController.getPlayers().size)
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun logoutEvent(e: PlayerDisconnectEvent) {
        cordexProxy.playerController.removePlayer(e.player)
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun loginEvent(e: LoginEvent) {
        e.registerIntent(cordexProxy)
        cordexProxy.proxy.scheduler.runAsync(cordexProxy) {
            cordexProxy.logController.log("LoginEvent", LogType.DEBUG)

            val ipStorage = cordexProxy.cacheController.getIpData(e.connection.address.address.hostAddress)
            val playerStorage = cordexProxy.cacheController.getPlayerData(e.connection.uniqueId)
            var banStorage = cordexProxy.cacheController.getBanData(ipStorage.id, null)
            if (playerStorage != null) {
                banStorage = cordexProxy.cacheController.getBanData(ipStorage.id, playerStorage.id)

                if (Inet4Address.getLocalHost().hostAddress.startsWith("82.208")) {
                    e.connection.disconnect(*TextComponent.fromLegacyText("&b&lProsim pripoj se pres nasi novou IP adresu\n\n&fNase nova IP adresa: &emc.minecord.cz".colored()))
                }

                if (cordexProxy.cacheController.getConfigValue("whitelist").asBoolean() && !playerStorage.isWhitelisted && !playerStorage.rank.isAdmin) {
                    val firstLine = cordexProxy.translationController.getTranslation(ipStorage.language, "maintenanceMotdFirstLine").colored()
                    val secondLine = cordexProxy.translationController.getTranslation(ipStorage.language, "maintenanceMotdSecondLine").colored()
                    e.connection.disconnect(*TextComponent.fromLegacyText((firstLine + "\n\n" + secondLine).colored()))
                }
            }

            if (banStorage != null)
                e.connection.disconnect(*TextComponent.fromLegacyText(cordexProxy.translationController.getTranslation(ipStorage.language, "bannedDisconnect").colored().replace("%reason%", banStorage.reason).replace("%expire%", banStorage.expire.toString()).replace("\\n", "\n")))

            e.completeIntent(cordexProxy)
        }
    }
}
