package net.minecord.cordexproxy.command

import club.minnced.discord.webhook.WebhookClientBuilder
import club.minnced.discord.webhook.send.WebhookEmbed
import club.minnced.discord.webhook.send.WebhookEmbedBuilder
import club.minnced.discord.webhook.send.WebhookMessageBuilder
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.minecord.cordexproxy.CordexProxy
import net.minecord.cordexproxy.model.controller.player.CordPlayer
import net.minecord.cordexproxy.model.controller.player.PlayerStorage
import net.minecord.cordexproxy.util.colored
import java.lang.NullPointerException
import java.util.*


class ReportCommand(cordexProxy: CordexProxy, name: String, permission: String, private val webHook: String) : BaseCommand(cordexProxy, name, permission) {
    override fun execute(commandSender: CommandSender, args: Array<String>) {
        val cordPlayer = cordexProxy.playerController.getPlayer(commandSender as ProxiedPlayer)

        if (args.size < 2) {
            cordPlayer.sendMessage("report", cordPlayer.translateMessage("reportInfo"))
            return
        }

        val target: PlayerStorage? = cordexProxy.databaseController.loadPlayerData(args[0])
        if (target == null) {
            val message = cordPlayer.translateMessage("reportExistError").replace("%player%", args[0])
            cordPlayer.sendMessage("report", message)
            return
        }

        val isAbleToReport: Boolean = cordexProxy.databaseController.isAbleToReportPlayer(cordPlayer.data.id)

        if (!isAbleToReport) {
            val message = cordPlayer.translateMessage("reportDelay")
            cordPlayer.sendMessage("report", message)
            return
        }

        var reason = java.lang.StringBuilder()
        for (i in 1 until args.size) {
            reason.append(args[i]).append(" ")
        }

        reason = StringBuilder(reason.substring(0, reason.length - 1))

        cordexProxy.databaseController.reportPlayer(target.id, cordPlayer.data.id, reason.toString())

        val builder = WebhookClientBuilder(webHook)

        builder.setThreadFactory { job: Runnable? ->
            val thread = Thread(job)
            thread.name = "Hello"
            thread.isDaemon = true
            thread
        }
        builder.setWait(true)

        val client = builder.build()

        val messageBuilder = WebhookMessageBuilder()

        var targetPlayer: CordPlayer? = null
        var lastMessages = ""
        if (!listOf("Cheating", "Griefing", "Fly", "Bugging").contains(reason.toString())) {
            try {
                targetPlayer = cordexProxy.playerController.getPlayerByUniqueId(target.uuid)
                if (targetPlayer.lastMessages.isNotEmpty()) {
                    lastMessages += "\n\nLast 3 messages:"
                    for (message in targetPlayer.lastMessages.takeLast(3)) {
                        lastMessages += "\n" + message
                    }
                }
            } catch (e: NullPointerException) {
            }
        }

        val embed = WebhookEmbedBuilder()
                .setColor(target.rank.integerColor)
                .setAuthor(WebhookEmbed.EmbedAuthor(target.name, "https://minotar.net/helm/" + target.name + "/96", null))
                .setDescription("""
                        Playtime: ${target.getFormattedPlayedTime(true)}
                        Current IP address: **""" + target.lastIpAddress + "**" +
                        "\nFirst login: **" + target.firstLogin + "**" +
                        "\nOriginal minecraft: **" + (if (target.type == "online") "yes" else "no") + "**" +
                        "\nBanned: **" + cordexProxy.databaseController.getBanCount(target.id) + "x** | Reported: **" + cordexProxy.databaseController.getReportCount(target.id) + "x**" +
                        "\n**-----------------------------------------------------------------------------**" + lastMessages +
                        "\n\nReport reason: **" + reason + "**" +
                        "\nReported on server: **" + ChatColor.stripColor(cordexProxy.serverController.getServer(cordPlayer.player.server.info.name).displayName.colored()) + "**"
                )
                .setThumbnailUrl("https://minotar.net/helm/" + target.name + "/96")
                .build()

        messageBuilder.setAvatarUrl("https://minotar.net/helm/" + cordPlayer.player.name + "/96")
        messageBuilder.setUsername(cordPlayer.player.name)
        messageBuilder.addEmbeds(listOf(embed))

        client.send(messageBuilder.build())

        cordPlayer.sendMessage("report", cordPlayer.translateMessage("reportSuccess")
                .replace("%player%", args[0])
                .replace("%rcolor%", target.rank.stringColor))

        if (targetPlayer != null && reason.toString().toLowerCase().contains("spam", true) && !cordexProxy.banController.isMuted(targetPlayer.player.uniqueId)) {
            val spamNumber = 3
            val muteMinutes = 20
            val reportsNeeded = 3

            val prepareMessage = fun () {
                messageBuilder.resetEmbeds()
                messageBuilder.setUsername("Minecord AI")
                messageBuilder.setAvatarUrl("https://minecord.cz/img/ai.jpg")
            }

            for (message in targetPlayer.lastMessages) {
                if (Collections.frequency(targetPlayer.lastMessages, message) >= spamNumber) {
                    cordexProxy.banController.mutePlayer(targetPlayer, reason.toString(), 60 * muteMinutes)
                    targetPlayer.lastMessages.clear()
                    prepareMessage()
                    messageBuilder.setContent(
                            "Hrac **${target.name}** byl umlcen za **Spam** na **$muteMinutes minut**" +
                                    "\nSpamoval ${spamNumber}x text *$message*"
                    )
                    client.send(messageBuilder.build())
                    return
                }
            }

            if (cordexProxy.databaseController.getReportCountInLastTenMinutes(target.id) > reportsNeeded) {
                cordexProxy.banController.mutePlayer(targetPlayer, reason.toString(), 60 * muteMinutes)
                targetPlayer.lastMessages.clear()
                prepareMessage()
                messageBuilder.setContent(
                        "Hrac **${target.name}** byl umlcen za **Spam** na **$muteMinutes minut** za vice nez $reportsNeeded reporty"
                )
                client.send(messageBuilder.build())
                return
            }
        }
    }

    override fun onTabComplete(sender: CommandSender?, args: Array<out String>?): MutableIterable<String> {
        val list = mutableListOf<String>()
        var bypass = false

        if (sender is ProxiedPlayer) {
            if (cordexProxy.playerController.getPlayer(sender).rank.isAdmin) {
                bypass = true
            }
        }

        if (args != null) {
            when (args.size) {
                1 -> for (player in cordexProxy.playerController.getPlayers()) {
                    if ((!player.hidden || bypass) && (player.player.name.startsWith(args[0], true) || player.player.name.contains(args[0], true)))
                        list.add(player.player.name)
                }
                2 -> {
                    val reasons = listOf("Spam", "Swearing", "Insulting", "Griefing", "Cheating")
                    for (reason in reasons) {
                        if (reason.startsWith(args[1], true))
                            list.add(reason)
                    }
                }
            }
        }

        return list
    }
}
