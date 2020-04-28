package net.minecord.cordexproxy.command

import net.minecord.cordexproxy.CordexProxy
import net.minecord.cordexproxy.model.controller.player.CordPlayer
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.connection.ProxiedPlayer

class UnbanCommand(cordexProxy: CordexProxy, name: String, permission: String, vararg aliases: String) : BaseCommand(cordexProxy, name, permission, *aliases) {
    override fun execute(commandSender: CommandSender, args: Array<String>) {
        var admin: CordPlayer? = null
        if (commandSender is ProxiedPlayer) {
            if (!commandSender.hasPermission("cordex.ban"))
                return
            admin = cordexProxy.playerController.getPlayer(commandSender)
        }

        if (args.isEmpty()) {
            admin?.sendMessage("banlist", "%prefix% Example unban: &e/unban Nick &7more at &b/cordex ban")
            return
        }

        val target = cordexProxy.cacheController.getPlayerData(args[0])
        if (target == null) {
            admin?.sendMessage("banlist", "%prefix% Player not found, manual is at &b/cordex ban")
            return
        }

        val banStorage = cordexProxy.cacheController.getBanData(target.lastIp, target.id)
        if (banStorage == null) {
            admin?.sendMessage("banlist", "%prefix% Player &e" + target.name + " &7not found in ban database")
        } else {
            admin?.sendMessage("banlist", "%prefix% Player &a" + target.name + " &7successfully unbanned")
        }

        cordexProxy.cacheController.removeBanData(target.id, target.lastIp, target.name)
        cordexProxy.banController.removeBan(target.id, target.lastIp)
    }

    override fun onTabComplete(sender: CommandSender?, args: Array<out String>?): MutableIterable<String> {
        val list = mutableListOf<String>()

        if (args != null) {
            when {
                args.size == 1 -> for (nickname in cordexProxy.cacheController.bannedNicknames) {
                    if (nickname.contains(args[0], true))
                        list.add(nickname)
                }
            }
        }

        return list
    }
}
