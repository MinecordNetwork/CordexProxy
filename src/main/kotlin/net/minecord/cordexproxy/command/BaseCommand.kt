package net.minecord.cordexproxy.command

import net.minecord.cordexproxy.CordexProxy
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.plugin.Command
import net.md_5.bungee.api.plugin.TabExecutor

open class BaseCommand internal constructor(var cordexProxy: CordexProxy, name: String, permission: String, vararg aliases: String) : Command(name, permission, *aliases), TabExecutor {
    override fun execute(commandSender: CommandSender, args: Array<String>) {

    }

    override fun onTabComplete(sender: CommandSender?, args: Array<out String>?): MutableIterable<String> {
        return mutableListOf()
    }
}
