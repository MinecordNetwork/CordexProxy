package net.minecord.cordexproxy.listener

import net.minecord.cordexproxy.CordexProxy
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.event.LoginEvent
import net.md_5.bungee.api.event.PlayerDisconnectEvent
import net.md_5.bungee.api.event.PostLoginEvent
import net.md_5.bungee.event.EventHandler
import net.md_5.bungee.event.EventPriority
import net.minecord.cordexproxy.model.controller.translation.LanguageType
import net.minecord.cordexproxy.util.colored

class ConnectionListener(cordexProxy: CordexProxy) : BaseListener(cordexProxy) {
    @EventHandler(priority = EventPriority.LOWEST)
    fun postLoginEvent(e: PostLoginEvent) {
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
            var successfulConnection = true
            val ipStorage = cordexProxy.cacheController.getIpData(e.connection.address.address.hostAddress)
            var playerStorage = cordexProxy.cacheController.getPlayerData(e.connection.uniqueId)
            var banStorage = cordexProxy.cacheController.getBanData(ipStorage.id, null)

            if (playerStorage == null) {
                for (i in 0..30) {
                    Thread.sleep(100)
                    playerStorage = cordexProxy.cacheController.getPlayerData(e.connection.uniqueId)
                    if (playerStorage != null) {
                        break
                    }
                }
            }

            if (ipStorage.country == "TN") {
                e.connection.disconnect(*TextComponent.fromLegacyText("Internal Exception: java.io.IOException: An existing connection was forcibly closed, java version mismatch"))
                successfulConnection = false
            }

            if (e.connection.version < 763) {
                if (ipStorage.language == LanguageType.CS) {
                    e.connection.disconnect(*TextComponent.fromLegacyText("&b&lMinimalni pozadovana verze hry je &e&l1.20\n\n&fStarsi verze minecraftu nepodporujeme".colored()))
                    successfulConnection = false
                } else {
                    e.connection.disconnect(*TextComponent.fromLegacyText("&b&lYou need to use at least version &e&l1.20\n\n&fOlder versions are not supported on our server".colored()))
                    successfulConnection = false
                }
            }

            if (playerStorage != null) {
                cordexProxy.botProtectManager.check()

                if (cordexProxy.botProtectManager.isBlocked(playerStorage, ipStorage)) {
                    e.connection.disconnect(*TextComponent.fromLegacyText("Sorry, try again later"))
                    successfulConnection = false

                } else if (cordexProxy.botProtectManager.isMaxIpConnectionsExceeded(ipStorage)) {
                    e.connection.disconnect(*TextComponent.fromLegacyText(cordexProxy.translationController.getTranslation(ipStorage.language, "ipMaxConnectionsExceeded")))
                    successfulConnection = false
                }

                banStorage = cordexProxy.cacheController.getBanData(ipStorage.id, playerStorage.id)

                if (cordexProxy.cacheController.getConfigValue("whitelist").asBoolean() && !playerStorage.isWhitelisted && !playerStorage.rank.isAdmin) {
                    val firstLine = cordexProxy.translationController.getTranslation(ipStorage.language, "maintenanceMotdFirstLine").colored()
                    val secondLine = cordexProxy.translationController.getTranslation(ipStorage.language, "maintenanceMotdSecondLine").colored()
                    e.connection.disconnect(*TextComponent.fromLegacyText((firstLine + "\n\n" + secondLine).colored()))
                    successfulConnection = false
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
                successfulConnection = false
            }

            if (successfulConnection) {
                cordexProxy.botProtectManager.onSuccessfulConnection(ipStorage)
            }

            e.completeIntent(cordexProxy)
        }
    }
}
