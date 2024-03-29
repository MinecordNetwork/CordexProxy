package net.minecord.cordexproxy.listener

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.md_5.bungee.api.Favicon
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.event.ProxyPingEvent
import net.md_5.bungee.event.EventHandler
import net.minecord.cordexproxy.CordexProxy
import net.minecord.cordexproxy.model.controller.chat.MotdStorage
import net.minecord.cordexproxy.model.controller.chat.MotdType
import net.minecord.cordexproxy.util.centerMotdMessage
import net.minecord.cordexproxy.util.colored
import java.io.File
import java.io.IOException
import java.net.Inet4Address
import javax.imageio.ImageIO
import kotlin.random.Random

class PingListener(cordexProxy: CordexProxy) : BaseListener(cordexProxy) {
    private val legacyComponentSerializer = LegacyComponentSerializer.builder().character('&').hexCharacter('#').hexColors().build()

    @EventHandler
    fun onProxyPing(event: ProxyPingEvent) {
        val ipAddress = event.connection.address.address.hostAddress
        val ipData = cordexProxy.cacheController.getIpData(ipAddress)
        val languageType = ipData.language
        var firstLine: String
        var secondLine: String

        val banData = cordexProxy.cacheController.getBanData(ipData.id, 0)
        val networkName = cordexProxy.translationController.getTranslation(languageType, "serverName").colored()
        if (banData == null) {
            if (cordexProxy.cacheController.getConfigValue("whitelist").asBoolean()) {
                firstLine = cordexProxy.translationController.getTranslation(languageType, "maintenanceMotdFirstLine").replace("%network%", networkName).colored().centerMotdMessage()
                secondLine = cordexProxy.translationController.getTranslation(languageType, "maintenanceMotdSecondLine").replace("%network%", networkName).colored().centerMotdMessage()

                try {
                    event.response.setFavicon(Favicon.create(ImageIO.read(File("maintenance-icon.png"))))
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            } else {
                val motds: List<MotdStorage>? = if (cordexProxy.cacheController.getConfigValue("fakaheda_ping_ip").toString() != ipAddress)
                    cordexProxy.chatController.getMotds(languageType, MotdType.REFRESH)
                else
                    cordexProxy.chatController.getMotds(languageType, MotdType.FAKAHEDA)

                val motdStorage = motds!![Random.nextInt(motds.size)]

                firstLine = legacyComponentSerializer.serialize(Component.text(motdStorage.firstPayload.replace("%country%", ipData.country.toLowerCase()).replace("%language%", ipData.language.toString().toLowerCase()).replace("%network%", networkName))).colored()
                secondLine = legacyComponentSerializer.serialize(Component.text(motdStorage.secondPayload.replace("%country%", ipData.country.toLowerCase()).replace("%language%", ipData.language.toString().toLowerCase()).replace("%network%", networkName))).colored()

                /*val firstLineRgb = legacyComponentSerializer.serialize(Component.text(firstLine))
                val secondLineRgb = legacyComponentSerializer.serialize(Component.text(secondLine))*/

                for (serverInfo in ProxyServer.getInstance().servers.values) {
                    firstLine = firstLine.replace("{players " + serverInfo.name + "}", serverInfo.players.size.toString() + "")
                    secondLine = secondLine.replace("{players " + serverInfo.name + "}", serverInfo.players.size.toString() + "")
                }

                if (motdStorage.isFirstCentered)
                    firstLine = firstLine.centerMotdMessage()

                if (motdStorage.isSecondCentered)
                    secondLine = secondLine.centerMotdMessage()

                val iconName = if (ipData.country == "SK" || ipData.country == "CZ")
                    "server-icon-local.png"
                else
                    "server-icon.png"

                try {
                    event.response.setFavicon(Favicon.create(ImageIO.read(File(iconName))))
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

            event.response.players.max = cordexProxy.cacheController.getPlayerRecord() + 1

        } else {
            firstLine = (cordexProxy.translationController.getTranslation(languageType, "banMotdFirstLine").replace("%expire%", banData.getFriendlyLeftTime()).replace("%reason%", banData.reason).replace("%network%", networkName)).colored()
            secondLine = (cordexProxy.translationController.getTranslation(languageType, "banMotdSecondLine").replace("%expire%", banData.getFriendlyLeftTime()).replace("%reason%", banData.reason).replace("%network%", networkName)).colored()

            event.response.players.max = Random.nextInt(1000)
            event.response.players.online = Random.nextInt(1000)

            try {
                /*if (ipData.country == "SK" || ipData.country == "CZ") {
                    event.response.setFavicon(Favicon.create(ImageIO.read(File("ban-icon-sms_${ipData.country}.png"))))
                } else {*/
                    event.response.setFavicon(Favicon.create(ImageIO.read(File("ban-icon.png"))))
               /*}*/
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        if (Inet4Address.getLocalHost().hostAddress.startsWith("82.208")) {
            event.response.players.online = cordexProxy.serverController.getPlayerCount()
        }

        event.response.descriptionComponent = ComponentBuilder(firstLine + "\n" + secondLine).create()[0]
    }
}
