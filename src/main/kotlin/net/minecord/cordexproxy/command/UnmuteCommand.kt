package net.minecord.cordexproxy.command

import net.minecord.cordexproxy.CordexProxy
import net.minecord.cordexproxy.model.controller.player.CordPlayer
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.connection.ProxiedPlayer

class UnmuteCommand(cordexProxy: CordexProxy, name: String, permission: String, vararg aliases: String) : BaseCommand(cordexProxy, name, permission, *aliases) {
    override fun execute(commandSender: CommandSender, args: Array<String>) {
        var admin: CordPlayer? = null
        if (commandSender is ProxiedPlayer) {
            if (!commandSender.hasPermission("cordex.mute"))
                return
            admin = cordexProxy.playerController.getPlayer(commandSender)
        }

        if (args.isEmpty()) {
            admin?.sendMessage("banlist", "%prefix% Example unban: &e/unmute Nick")
            return
        }

        val target = cordexProxy.cacheController.getPlayerData(args[0])
        if (target == null) {
            admin?.sendMessage("banlist", "%prefix% Player not found, manual is at &b/cordex ban")
            return
        }

        if (cordexProxy.banController.isMuted(target.uuid)) {
            cordexProxy.banController.unmutePlayer(target.uuid)
            admin?.sendMessage("banlist", "%prefix% Player &a" + target.name + " &7successfully unmuted")
        } else {
            admin?.sendMessage("banlist", "%prefix% Player &e" + target.name + " &7is not muted")
        }
    }

    override fun onTabComplete(sender: CommandSender?, args: Array<out String>?): MutableIterable<String> {
        val mutedNicknames = mutableListOf<String>()
        cordexProxy.banController.mutedPlayers.values.forEach { mutedNicknames.add(it.playerName) }
        val list = mutableListOf<String>()

        if (args != null) {
            when (args.size) {
                1 -> for (nickname in mutedNicknames) {
                    if (nickname.contains(args[0], true))
                        list.add(nickname)
                }
            }
        }

        return list
    }
}
