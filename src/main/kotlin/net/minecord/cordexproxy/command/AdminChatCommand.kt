package net.minecord.cordexproxy.command

import net.minecord.cordexproxy.CordexProxy
import net.minecord.cordexproxy.model.controller.player.CordPlayer
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.connection.ProxiedPlayer

class AdminChatCommand(cordexProxy: CordexProxy, name: String, permission: String, vararg aliases: String) : BaseCommand(cordexProxy, name, permission, *aliases) {
    override fun execute(commandSender: CommandSender, strings: Array<String>) {
        if (strings.isEmpty())
            return

        var admin: CordPlayer? = null
        if (commandSender is ProxiedPlayer) {
            admin = cordexProxy.playerController.getPlayer(commandSender)
            if (!admin.rank.isAdmin)
                return
        }

        var message = StringBuilder()
        for (string in strings)
            message.append(string).append(" ")

        message = StringBuilder(cordexProxy.chatController.fixMessage(message.substring(0, message.length - 1)))

        var serverName = ""
        if (admin != null) {
            serverName = "&b[" + cordexProxy.serverController.getServer(admin.player.server.info.name).displayName + "] "
        }

        for (cordPlayer in cordexProxy.playerController.getPlayers()) {
            if (cordPlayer.rank.isAdmin) {
                if (admin != null) {
                    if (admin.player.server.info.name == cordPlayer.player.server.info.name)
                        cordPlayer.sendMessage("adminchat", "%prefix% &a" + admin.player.name + " &f» &e" + message.toString())
                    else
                        cordPlayer.sendMessage("adminchat", "%prefix% " + serverName + "&a" + admin.player.name + " &f» &e" + message.toString())
                } else
                    cordPlayer.sendMessage("adminchat", "%prefix% &aServer &f» &e$message")
            }
        }

        if (admin != null)
            cordexProxy.logController.write(cordexProxy.chatController.getPrefix("adminchat") + " " + serverName + "&a" + admin.player.name + " &f» &e" + message.toString())
        else
            cordexProxy.logController.write(cordexProxy.chatController.getPrefix("adminchat") + " " + serverName + "&aServer &f» &e" + message.toString())
    }
}
