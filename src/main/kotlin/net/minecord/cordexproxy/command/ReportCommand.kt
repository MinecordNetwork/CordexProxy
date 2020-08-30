package net.minecord.cordexproxy.command

import club.minnced.discord.webhook.WebhookClientBuilder
import club.minnced.discord.webhook.send.WebhookEmbed
import club.minnced.discord.webhook.send.WebhookEmbedBuilder
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.minecord.cordexproxy.CordexProxy
import net.minecord.cordexproxy.model.controller.player.PlayerStorage
import net.minecord.cordexproxy.util.colored
import org.bukkit.ChatColor


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

        val embed = WebhookEmbedBuilder()
                .setColor(0xFF00EE)
                .setAuthor(WebhookEmbed.EmbedAuthor(cordPlayer.player.name, "https://minotar.net/avatar/" + target.name + "/96", null))
                .setDescription("""Played time: ${target.getFormattedPlayedTime(true)}
                        Player ID and UUID: ${target.id} | """ + target.uuid +
                        "\nCurrent IP address: **" + target.lastIpAddress + "**" +
                        "\nFirst log-in: **" + target.firstLogin + "**" +
                        "\nPlayer type: **" + target.type + "**" +
                        "\nBanned: **" + cordexProxy.databaseController.getBanCount(target.id) + "x**" +
                        "\n\nReport reason: **" + reason + "**" +
                        "\nReported on server: **" + ChatColor.stripColor(cordexProxy.serverController.getServer(cordPlayer.player.server.info.name).displayName.colored()) + "**"
                )
                .setThumbnailUrl("https://minotar.net/avatar/" + target.name + "/96")
                .build()

        client.send(embed)
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
                    val reasons = listOf("Spam", "Swearing", "Insulting", "Griefing")
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
