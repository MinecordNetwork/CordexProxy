package net.minecord.cordexproxy.command

import net.md_5.bungee.api.ProxyServer
import net.minecord.cordexproxy.CordexProxy
import net.minecord.cordexproxy.model.controller.player.CordPlayer
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.connection.ProxiedPlayer

class MuteCommand(cordexProxy: CordexProxy, name: String, permission: String, vararg aliases: String) : BaseCommand(cordexProxy, name, permission, *aliases) {
    override fun execute(commandSender: CommandSender, args: Array<String>) {
        var admin: CordPlayer? = null
        if (commandSender is ProxiedPlayer) {
            if (!commandSender.hasPermission("cordex.mute"))
                return
            admin = cordexProxy.playerController.getPlayer(commandSender)
        }

        if (args.size < 3) {
            admin?.sendMessage("banlist", "%prefix% Example mute: &e/mute Nick Swearing 20m &7more at &b/cordex ban")
            return
        }

        val proxiedPlayer = ProxyServer.getInstance().getPlayer(args[0])
        if (proxiedPlayer == null) {
            admin?.sendMessage("banlist", "%prefix% Player not found, manual is at &b/cordex ban")
            return
        }

        val target = cordexProxy.playerController.getPlayer(ProxyServer.getInstance().getPlayer(args[0]))

        var reason = StringBuilder()
        for (i in 1 until args.size - 1)
            reason.append(args[i]).append(" ")

        reason = StringBuilder(reason.substring(0, reason.length - 1))

        val howLong = args[args.size - 1]

        var duration = Integer.valueOf(howLong.substring(0, howLong.length - 1))

        when {
            howLong.endsWith("m") -> duration *= 60
            howLong.endsWith("d") -> duration *= 3600 * 24
            howLong.endsWith("h") -> duration *= 3600
            howLong.endsWith("w") -> duration *= 3600 * 24 * 7
            howLong.endsWith("M") -> duration *= 3600 * 24 * 30
            howLong.toUpperCase().endsWith("Y") -> duration *= 3600 * 24 * 365
        }

        cordexProxy.banController.mutePlayer(target, reason.toString(), duration)
    }

    override fun onTabComplete(sender: CommandSender?, args: Array<out String>?): MutableIterable<String> {
        val list = mutableListOf<String>()

        if (args == null || args.size == 1) {
            for (player in ProxyServer.getInstance().players) {
                list.add(player.name)
            }
        } else if (args.size == 2) {
            list.add("Insulting")
            list.add("Swearing")
            list.add("Advertisement")
            list.add("Spam")
            list.add("Stupidity")
        } else if (args.size == 3) {
            list.add("15m")
            list.add("30m")
            list.add("1h")
            list.add("2h")
            list.add("4h")
        }

        return list
    }
}
