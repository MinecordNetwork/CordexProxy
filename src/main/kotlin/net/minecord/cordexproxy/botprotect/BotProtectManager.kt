package net.minecord.cordexproxy.botprotect

import club.minnced.discord.webhook.send.WebhookEmbedBuilder
import club.minnced.discord.webhook.send.WebhookMessageBuilder
import net.md_5.bungee.api.chat.TextComponent
import net.minecord.cordexproxy.CordexProxy
import net.minecord.cordexproxy.model.controller.log.LogType
import net.minecord.cordexproxy.model.controller.player.IpStorage
import net.minecord.cordexproxy.model.controller.player.PlayerStorage
import net.minecord.cordexproxy.util.colored
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.roundToInt

class BotProtectManager(private val cordexProxy: CordexProxy) {
    private var botProtectionEnabledTo: Long = 0
    private var connectionsPerMinute = ConcurrentHashMap<IpStorage, Long>()

    private fun calculateIpPercent(ipStorages: Collection<IpStorage>): Double {
        var czechOrSlovak = 0
        var foreign = 0

        for (ipStorage in ipStorages) {
            if (ipStorage.country == "SK" || ipStorage.country == "CZ") {
                czechOrSlovak++
            } else {
                foreign++
            }
        }

        val total = (czechOrSlovak + foreign)
        if (total == 0) {
            return 0.0
        }

        return foreign / total.toDouble()
    }

    private fun getForeignIpPercent(): Double {
        return calculateIpPercent(cordexProxy.playerController.getPlayers().map { it.ipData })
    }

    private fun getForeignIpPercentFromConnections(): Double {
        return calculateIpPercent(connectionsPerMinute.keys)
    }

    fun onSuccessfulConnection(ipStorage: IpStorage) {
        for ((connection, time) in connectionsPerMinute) {
            if (time + 60 * 1000 < System.currentTimeMillis()) {
                connectionsPerMinute.remove(connection)
            }
        }

        connectionsPerMinute[ipStorage] = System.currentTimeMillis()
    }

    fun check() {
        cordexProxy.logController.log("Percent: ${getForeignIpPercent()}", LogType.DEBUG)

        if (connectionsPerMinute.size >= 12) {
            if (getForeignIpPercentFromConnections() >= 0.5) {
                enableBotProtection(3, true, "More than 6 connections per minute from foreign IPs")
            }
        }

        if (cordexProxy.playerController.getPlayers().size >= 32 && getForeignIpPercent() > 0.3) {
            enableBotProtection(3, true, "More than 30% players have foreign IPs")

        } else if (cordexProxy.playerController.getPlayers().size >= 16 && getForeignIpPercent() > 0.4) {
            enableBotProtection(2, true, "More than 30% players have foreign IPs")

        } else if (cordexProxy.playerController.getPlayers().size >= 10 && getForeignIpPercent() > 0.7) {
            enableBotProtection(1, true, "More than 70% players have foreign IPs")
        }
    }

    fun isBlocked(playerStorage: PlayerStorage, ipStorage: IpStorage): Boolean {
        if (isProtectionActive()) {
            if (ipStorage.country != "CZ" && ipStorage.country != "SK" && playerStorage.playedTime < 1000) {
                return true
            }
        }

        return false
    }

    fun isMaxIpConnectionsExceeded(ipStorage: IpStorage): Boolean {
        val connections = cordexProxy.playerController.getPlayers().filter { it.data.lastIp != 0 && it.data.lastIp == ipStorage.id }.size
        if (connections > 2) {
            return true
        }

        return false
    }

    fun isProtectionActive(): Boolean {
        return System.currentTimeMillis() <= botProtectionEnabledTo
    }

    fun enableBotProtection(hours: Int, automatic: Boolean = false, reason: String = "none") {
        if (automatic && isProtectionActive()) {
            return
        }

        if (!isProtectionActive()) {
            cordexProxy.proxy.scheduler.runAsync(cordexProxy) {
                for (i in 0..3) {
                    Thread.sleep(1000)
                    for (player in cordexProxy.playerController.getPlayers()) {
                        if (player.ipData.country != "CZ" && player.ipData.country != "SK" && player.data.playedTime < 1000) {
                            player.player.disconnect(*TextComponent.fromLegacyText("Try again later"))
                        }
                    }
                }
            }

            for (player in cordexProxy.playerController.getPlayers()) {
                player.sendActionBar(player.translateMessage("botProtectionActivated").colored())
            }
        }

        botProtectionEnabledTo = (hours * 3600 * 1000) + System.currentTimeMillis()

        val client = cordexProxy.discordWebhookClientProvider.urgentWebhookClient

        val messageBuilder = WebhookMessageBuilder()

        val embed = WebhookEmbedBuilder()
            .setDescription("""
                        Players online: ${cordexProxy.playerController.getPlayers().size}
                        Foreign IPs online: ${(getForeignIpPercent()*100).roundToInt()}%
                        Foreign IPs per last minute: ${(getForeignIpPercentFromConnections()*100).roundToInt()}%
                        Reason: $reason
                        Automatic: $automatic
                        Hours: $hours
                        Player list: ${cordexProxy.playerController.getPlayers().sortedBy { it.player.name }.joinToString { it.player.name }}"""
            )
            .setThumbnailUrl("https://minotar.net/helm/FatLui/96")
            .build()

        messageBuilder.setAvatarUrl("https://minotar.net/helm/FatLui/96")
        messageBuilder.setUsername("BotProtect")
        messageBuilder.addEmbeds(listOf(embed))

        client.send(messageBuilder.build())
    }

    fun disableBotProtection() {
        botProtectionEnabledTo = 0
    }
}
