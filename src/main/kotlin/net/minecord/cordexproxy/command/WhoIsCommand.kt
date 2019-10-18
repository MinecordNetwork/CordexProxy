package net.minecord.cordexproxy.command

import net.md_5.bungee.api.ProxyServer
import net.minecord.cordexproxy.CordexProxy
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.minecord.cordexproxy.model.controller.player.CordPlayer
import net.minecord.cordexproxy.util.formatTime

class WhoIsCommand(cordexProxy: CordexProxy, name: String, permission: String, vararg aliases: String) : BaseCommand(cordexProxy, name, permission, *aliases) {
    override fun execute(commandSender: CommandSender, args: Array<String>) {
        var admin: CordPlayer? = null
        if (commandSender is ProxiedPlayer) {
            if (!commandSender.hasPermission("cordex.whois"))
                return
            admin = cordexProxy.playerController.getPlayer(commandSender)
        }

        if (args.isEmpty())
            return

        val playerStorage = cordexProxy.databaseController.loadPlayerData(args[0])
        if (playerStorage == null) {
            admin?.sendMessage("system", "%prefix% Player ${args[0]} not found")
            return
        }

        val lastIp = cordexProxy.databaseController.loadIpData(playerStorage.lastIpAddress)

        admin?.sendMessage("system", "Who is &e${playerStorage.name}&7:")
        admin?.sendMessage(" &7ID: &f${playerStorage.id}")
        admin?.sendMessage(" &7UUID: &f${playerStorage.uuid}")
        admin?.sendMessage(" &7Rank: &f${playerStorage.rank.name}")
        admin?.sendMessage(" &7Country: &f${lastIp.country}")
        admin?.sendMessage(" &7First IP: &f${playerStorage.firstIpAddress}")
        admin?.sendMessage(" &7Latest IP: &f${playerStorage.lastIpAddress}")
        admin?.sendMessage(" &7Play time: &f${playerStorage.playedTime.formatTime()}")
        admin?.sendMessage(" &7First join: &f${playerStorage.firstJoin}")
        admin?.sendMessage(" &7Last join: &f${playerStorage.lastJoin}")
        admin?.sendMessage(" &7Online: &f${playerStorage.isOnline}")
        admin?.sendMessage(" &7Whitelisted: &f${playerStorage.isWhitelisted}")
        admin?.sendMessage(" &7Player type: &f${playerStorage.type}")
    }

    override fun onTabComplete(sender: CommandSender?, args: Array<out String>?): MutableIterable<String> {
        val list = mutableListOf<String>()

        if (args == null || args.size == 1) {
            for (player in ProxyServer.getInstance().players) {
                list.add(player.name)
            }
        }

        return list
    }
}
