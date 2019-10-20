package net.minecord.cordexproxy.command

import net.md_5.bungee.api.ProxyServer
import net.minecord.cordexproxy.CordexProxy
import net.minecord.cordexproxy.model.controller.player.CordPlayer
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.connection.ProxiedPlayer

class KickCommand : BaseCommand {
    constructor(cordexProxy: CordexProxy, name: String, permission: String, vararg aliases: String) : super(cordexProxy, name, permission, *aliases)

    override fun execute(commandSender: CommandSender, args: Array<String>) {
        var admin: CordPlayer? = null
        if (commandSender is ProxiedPlayer) {
            if (!commandSender.hasPermission("cordex.kick"))
                return
            admin = cordexProxy.playerController.getPlayer(commandSender)
        }

        if (args.size < 2) {
            admin?.sendMessage("banlist", "%prefix% Example kick: &e/kick Nick Swearing &7more at &b/cordex ban")
            return
        }

        val proxiedPlayer = ProxyServer.getInstance().getPlayer(args[0])
        if (proxiedPlayer == null) {
            admin?.sendMessage("banlist", "%prefix% Player not found, manual is at &b/cordex ban")
            return
        }

        val target = cordexProxy.playerController.getPlayer(ProxyServer.getInstance().getPlayer(args[0]))

        var reason = StringBuilder()
        for (i in 1 until args.size)
            reason.append(args[i]).append(" ")

        reason = StringBuilder(reason.substring(0, reason.length - 1))

        cordexProxy.banController.kickPlayer(target, reason.toString())
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
                    val reasons = listOf("Bugging", "Insulting", "Swearing", "Griefing", "Advertisement", "Spam", "Stupidity")
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
