package net.minecord.cordexproxy.command

import net.md_5.bungee.api.ProxyServer
import net.minecord.cordexproxy.CordexProxy
import net.minecord.cordexproxy.model.controller.player.CordPlayer
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.connection.ProxiedPlayer

class KickCommand : BaseCommand {
    constructor(cordexProxy: CordexProxy, name: String, permission: String, vararg aliases: String) : super(cordexProxy, name, permission, *aliases)

    override fun execute(commandSender: CommandSender, strings: Array<String>) {
        var admin: CordPlayer? = null
        if (commandSender is ProxiedPlayer) {
            if (!commandSender.hasPermission("cordex.kick"))
                return
            admin = cordexProxy.playerController.getPlayer(commandSender)
        }

        if (strings.size < 2) {
            admin?.sendMessage("banlist", "%prefix% Example kick: &e/kick Nick Swearing &7more at &b/cordex ban")
            return
        }

        val proxiedPlayer = ProxyServer.getInstance().getPlayer(strings[0])
        if (proxiedPlayer == null) {
            admin?.sendMessage("banlist", "%prefix% Player not found, manual is at &b/cordex ban")
            return
        }

        val target = cordexProxy.playerController.getPlayer(ProxyServer.getInstance().getPlayer(strings[0]))

        var reason = StringBuilder()
        for (i in 1 until strings.size)
            reason.append(strings[i]).append(" ")

        reason = StringBuilder(reason.substring(0, reason.length - 1))

        cordexProxy.banController.kickPlayer(target, reason.toString())
    }

    override fun onTabComplete(sender: CommandSender?, args: Array<out String>?): MutableIterable<String> {
        val list = mutableListOf<String>()

        if (args == null || args.size == 1) {
            for (player in ProxyServer.getInstance().players) {
                list.add(player.name)
            }
        } else if (args.size == 2) {
            list.add("Bugging")
            list.add("Insulting")
            list.add("Swearing")
            list.add("Griefing")
            list.add("Advertisement")
            list.add("Spam")
            list.add("Stupidity")
        }

        return list
    }
}
