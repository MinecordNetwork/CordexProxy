package net.minecord.cordexproxy.listener

import net.minecord.cordexproxy.CordexProxy
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.event.LoginEvent
import net.md_5.bungee.api.event.PlayerDisconnectEvent
import net.md_5.bungee.api.event.PostLoginEvent
import net.md_5.bungee.event.EventHandler
import net.md_5.bungee.event.EventPriority
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

            if (banStorage != null) {
                var text = cordexProxy.translationController.getTranslation(ipStorage.language, "bannedDisconnect").colored().replace("%reason%", banStorage.reason).replace("%expire%", banStorage.getFriendlyLeftTime()).replace("\\n", "\n")

                text = text.replace("%country%", ipStorage.country)
                text = text.replace("%nick%", banStorage.targetNick)

                if (ipStorage.country == "CZ") {
                    text = text.replace("%price%", "149")
                    text = text.replace("%number%", "90733")
                    text = text.replace("%price%", "149 Kc")
                } else if (ipStorage.country == "SK") {
                    text = text.replace("%price%", "6")
                    text = text.replace("%number%", "8877")
                    text = text.replace("%price%", "6,00 €")
                }

                e.connection.disconnect(*TextComponent.fromLegacyText(text))
            }

            e.completeIntent(cordexProxy)
        }
    }
}
