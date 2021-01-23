package net.minecord.cordexproxy.command

import net.minecord.cordexproxy.CordexProxy
import net.minecord.cordexproxy.model.controller.player.CordPlayer
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.connection.ProxiedPlayer

class BanCommand(cordexProxy: CordexProxy, name: String, permission: String, vararg aliases: String) : BaseCommand(cordexProxy, name, permission, *aliases) {
    override fun execute(commandSender: CommandSender, args: Array<String>) {
        val helpMessage = "%prefix% Example ban (7 days): &e/ipban Nick Cheating 7d &7more at &b/cordex ban"
        var admin: CordPlayer? = null

        if (commandSender is ProxiedPlayer) {
            if (!commandSender.hasPermission("cordex.ban"))
                return
            admin = cordexProxy.playerController.getPlayer(commandSender)
        }

        if (args.size < 3) {
            admin?.sendMessage("banlist", helpMessage)
            return
        }

        val target = cordexProxy.cacheController.getPlayerData(args[0])
        if (target == null) {
            admin?.sendMessage("banlist", "%prefix% Player not found, manual is at &b/cordex ban")
            return
        }

        var reason = StringBuilder()
        for (i in 1 until args.size - 1)
            reason.append(args[i]).append(" ")

        reason = StringBuilder(reason.substring(0, reason.length - 1))

        val howLong = args[args.size - 1]

        var duration = Integer.parseInt(howLong.substring(0, howLong.length - 1))

        when {
            howLong.endsWith("m") -> duration *= 60
            howLong.endsWith("d") -> duration *= 3600 * 24
            howLong.endsWith("h") -> duration *= 3600
            howLong.endsWith("w") -> duration *= 3600 * 24 * 7
            howLong.endsWith("M") -> duration *= 3600 * 24 * 30
            howLong.toUpperCase().endsWith("Y") -> duration *= 3600 * 24 * 365
        }

        admin?.sendMessage("banlist", "%prefix% Player &e" + target.name + " &7will be banned in 3s")

        cordexProxy.banController.insertBan(admin, target, reason.toString(), duration, true)
    }

    override fun onTabComplete(sender: CommandSender?, args: Array<out String>?): MutableIterable<String> {
        val list = mutableListOf<String>()

        if (args != null) {
            when {
                args.size == 1 -> for (player in ProxyServer.getInstance().players) {
                    if (player.name.startsWith(args[0], true) || player.name.contains(args[0], true))
                        list.add(player.name)
                }
                args.size == 2 -> {
                    val reasons = listOf("Cheating", "Flyhack", "Bugging", "Insulting", "Swearing", "Griefing", "Advertisement", "Spam", "Stupidity", "Multiaccount", "TeleportKill")
                    for (reason in reasons) {
                        if (reason.startsWith(args[1], true))
                            list.add(reason)
                    }
                }
                args.size == 3 -> {
                    val durations = listOf("1h", "4h", "2d", "7d", "14d", "12d", "30d")
                    for (duration in durations) {
                        if (duration.startsWith(args[2], true))
                            list.add(duration)
                    }
                }
            }
        }

        return list
    }
}
