package net.minecord.cordexproxy.command

import net.minecord.cordexproxy.CordexProxy
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.connection.ProxiedPlayer
import org.bukkit.command.ConsoleCommandSender

import java.util.ArrayList

class ProxyCommand(cordexProxy: CordexProxy, name: String, permission: String, vararg aliases: String) : BaseCommand(cordexProxy, name, permission, *aliases) {
    override fun execute(commandSender: CommandSender, strings: Array<String>) {
        val result = call(strings)

        if (commandSender is ProxiedPlayer) {
            if (!commandSender.hasPermission("cordex.proxy"))
                return
            val admin = cordexProxy.playerController.getPlayer(commandSender)
            for (message in result)
                admin!!.sendMessage(message.replace("%prefix%", cordexProxy.chatController.getPrefix("cordex")))
        } else if (commandSender is ConsoleCommandSender) {
            for (message in result)
                cordexProxy.logController.write(message.replace("%prefix%", cordexProxy.chatController.getPrefix("cordex")))
        }
    }

    private fun call(args: Array<String>): List<String> {
        val messages = ArrayList<String>()

        if (args.isEmpty()) {
            messages.add(" ")
            messages.add("&7The &eProxy &7manual:")
            messages.add(" &a/proxy whitelist <true/false> &f| &7Reload of whole plugin")
            messages.add(" &a/proxy reload &f| &7Reload of whole plugin")
            messages.add(" &a/proxy debug cache &f| &7Debug of cached objects")
            messages.add(" ")
        } else {
            when (args[0]) {
                "whitelist" -> if (args.size >= 2) {
                    cordexProxy.cacheController.setConfigValue("whitelist", args[1])
                    messages.add("%prefix% Whitelist set to &b" + args[1] + "&7, configuration was refreshed immediately")
                }
                "debug" -> if (args.size >= 2) {
                    when (args[1]) {
                        "cache" -> {
                            messages.add("%prefix% &6Cache controller debug:")
                            messages.addAll(cordexProxy.cacheController.debug())
                        }
                    }
                }
            }
        }

        return messages
    }
}
