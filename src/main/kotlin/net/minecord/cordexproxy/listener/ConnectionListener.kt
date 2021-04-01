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

            if (e.connection.version < 735) {
                if (ipStorage.language == LanguageType.CS) {
                    e.connection.disconnect(*TextComponent.fromLegacyText("&b&lMinimalni pozadovana verze hry je &e&l1.16\n\n&fStarsi verze minecraftu nepodporujeme".colored()))
                } else {
                    e.connection.disconnect(*TextComponent.fromLegacyText("&b&lYou need to use at least version &e&l1.16\n\n&fOlder versions are not supported on our server".colored()))
                }
            }

            /*val excludedNicks = cordexProxy.cacheController.getConfigValue("excluded_nicks").toString().split(",")
            if (e.connection.name !in excludedNicks) {
                if (e.connection.address.address.hostAddress.startsWith("85.160.") || e.connection.address.address.hostAddress.startsWith("89.24.")) {
                    e.connection.disconnect(*TextComponent.fromLegacyText("&c&lRozsah vasich IP adres byl zablokovan\n\n&fBylo zacate trestni konani tykajici se kradeni uctu\n\n&fVas poskytovatel internetu vas bude v nejblizsich dnech kontaktovat.\n\n&ePokud tvuj nick neni &bhoznik &ekontaktuj na na nasem discordu &bhttps://ds.minecord.cz".colored()))
                }
            }*/

            if (playerStorage != null) {
                /*if (ipStorage.country != "CZ" && ipStorage.country != "SK" && playerStorage.playedTime < 1000) {
                    e.connection.disconnect(*TextComponent.fromLegacyText("BYE"))
                }*/

                banStorage = cordexProxy.cacheController.getBanData(ipStorage.id, playerStorage.id)

                if (cordexProxy.cacheController.getConfigValue("whitelist").asBoolean() && !playerStorage.isWhitelisted && !playerStorage.rank.isAdmin) {
                    val firstLine = cordexProxy.translationController.getTranslation(ipStorage.language, "maintenanceMotdFirstLine").colored()
                    val secondLine = cordexProxy.translationController.getTranslation(ipStorage.language, "maintenanceMotdSecondLine").colored()
                    e.connection.disconnect(*TextComponent.fromLegacyText((firstLine + "\n\n" + secondLine).colored()))
                }
            } else if (ipStorage.country != "CZ" && ipStorage.country != "SK") {
                //e.connection.disconnect(*TextComponent.fromLegacyText("Easy bot attack protection"))
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
