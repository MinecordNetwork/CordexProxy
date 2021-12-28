package net.minecord.cordexproxy.botprotect

import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.minecord.cordexproxy.CordexProxy
import net.minecord.cordexproxy.command.BaseCommand

class BotProtectCommand(cordexProxy: CordexProxy, name: String, permission: String) : BaseCommand(cordexProxy, name, permission) {
    override fun execute(commandSender: CommandSender, args: Array<String>) {
        if (commandSender is ProxiedPlayer) {
            if (!commandSender.hasPermission("cordex.ban"))
                return
        }

        if (args.isEmpty()) {
            commandSender.sendMessage("Missing arguments")
            return
        }

        when {
            args[0] == "on" -> {
                var hours = if (args.size > 1) args[1].toInt() else 3
                if (hours == 0) {
                    hours = 3
                }
                cordexProxy.botProtectManager.enableBotProtection(hours, false, "Enabled by " + commandSender.name)

            }
            args[0] == "off" -> {
                cordexProxy.botProtectManager.disableBotProtection()

            }
            else -> {
                commandSender.sendMessage("Status: " + if (cordexProxy.botProtectManager.isProtectionActive()) "enabled" else "disabled")
            }
        }
    }

    override fun onTabComplete(sender: CommandSender?, args: Array<out String>?): MutableIterable<String> {
        val list = mutableListOf<String>()

        if (args != null) {
            when (args.size) {
                1 -> {
                    list.add("on")
                    list.add("off")
                    list.add("status")
                }
            }
        }

        return list
    }
}
