package net.minecord.cordexproxy.command

import net.minecord.cordexproxy.CordexProxy
import net.minecord.cordexproxy.model.controller.player.CordPlayer
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.chat.*
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.minecord.cordexproxy.util.colored

import java.util.HashMap

class FindCommand(cordexProxy: CordexProxy, name: String, permission: String, vararg aliases: String) : BaseCommand(cordexProxy, name, permission, *aliases) {
    override fun execute(commandSender: CommandSender, args: Array<String>) {
        if (args.isEmpty())
            return

        var cordPlayer: CordPlayer? = null
        if (commandSender is ProxiedPlayer) {
            cordPlayer = cordexProxy.playerController.getPlayer(commandSender)
        }

        val playerToFind = args[0]
        val target = cordexProxy.proxy.getPlayer(playerToFind)

        if (target == null) {
            val placeholders = HashMap<String, String>()

            placeholders["%rcolor%"] = "&f"
            placeholders["%player%"] = playerToFind

            if (cordPlayer != null)
                cordPlayer.sendMessage("find", cordPlayer.translateMessage("findFailed"), placeholders)
            else
                cordexProxy.proxy.console.sendMessage(*TextComponent.fromLegacyText("Player $playerToFind is offline"))

            return
        }

        val cordTarget = cordexProxy.playerController.getPlayer(target)
        val serverTarget = cordexProxy.serverController.getServer(target.server.info.name)

        if (cordPlayer != null) {
            val placeholders = HashMap<String, String>()

            placeholders["%rcolor%"] = cordTarget.rank.chatColor.toString()
            placeholders["%player%"] = target.name
            placeholders["%server%"] = serverTarget.displayName
            placeholders["%prefix%"] = cordexProxy.chatController.getPrefix("find")

            val message = arrayOf(cordPlayer.translateMessage("findSuccess"))
            placeholders.forEach { (key, value) -> message[0] = message[0].replace(key, value) }

            val msg = TextComponent(message[0].colored())

            msg.clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/server " + serverTarget.name)
            msg.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, ComponentBuilder((cordPlayer.translateMessage("connectToServer") + "\n" + cordPlayer.translateMessage("onlineFor").replace("%time%", cordTarget.data.getFormattedOnlineTime(true))).colored()).create())

            cordPlayer.sendLiveMessage(msg)
        } else
            cordexProxy.proxy.console.sendMessage(*TextComponent.fromLegacyText("Player " + target.name + " is online on server " + serverTarget.displayName))
    }

    override fun onTabComplete(sender: CommandSender?, args: Array<out String>?): MutableIterable<String> {
        val list = mutableListOf<String>()

        if (args != null) {
            when {
                args.size == 1 -> for (player in ProxyServer.getInstance().players) {
                    if (player.name.startsWith(args[0], true) || player.name.contains(args[0], true))
                        list.add(player.name)
                }
            }
        }

        return list
    }
}
