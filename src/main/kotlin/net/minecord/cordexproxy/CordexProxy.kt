package net.minecord.cordexproxy

import net.minecord.cordexproxy.command.*
import net.minecord.cordexproxy.controller.*
import net.minecord.cordexproxy.listener.*
import net.minecord.cordexproxy.model.controller.database.DatabaseCredentials
import net.md_5.bungee.api.plugin.Plugin
import net.md_5.bungee.config.Configuration
import net.md_5.bungee.config.ConfigurationProvider
import net.md_5.bungee.config.YamlConfiguration
import net.minecord.cordexproxy.botprotect.BotProtectCommand
import net.minecord.cordexproxy.botprotect.BotProtectManager
import net.minecord.cordexproxy.discord.DiscordWebhookClientProvider
import java.io.File

class CordexProxy : Plugin() {
    private val config: Configuration = ConfigurationProvider.getProvider(YamlConfiguration::class.java).load(File(dataFolder, "config.yml"))

    val databaseController = DatabaseController(this, DatabaseCredentials(config.getString("database.host"), config.getInt("database.port"), config.getString("database.name"), config.getString("database.user"), config.getString("database.pass")))
    val chatController = ChatController(this)
    val cacheController = CacheController(this)
    val translationController = TranslationController(this)
    val playerController = PlayerController(this)
    val serverController = ServerController(this)
    val rankController = RankController(this)
    val banController = BanController(this)

    val discordWebhookClientProvider by lazy { DiscordWebhookClientProvider(config.getString("webhook"), config.getString("webhookUrgent")) }
    val logController by lazy { LogController(this) }
    val utilController by lazy { UtilController(this) }
    val botProtectManager by lazy { BotProtectManager(this) }

    override fun onEnable() {
        if (!dataFolder.exists())
            dataFolder.mkdir()

        proxy.registerChannel("ProxyPrivateChannel")

        proxy.pluginManager.registerListener(this, PlayerListener(this))
        proxy.pluginManager.registerListener(this, GatewayListener(this))
        proxy.pluginManager.registerListener(this, PingListener(this))
        proxy.pluginManager.registerListener(this, SecurityListener(this))
        proxy.pluginManager.registerListener(this, ConnectionListener(this))
        proxy.pluginManager.registerListener(this, VoteListener(this))
        proxy.pluginManager.registerListener(this, VanishListener(this))

        proxy.pluginManager.registerCommand(this, AdminChatCommand(this, "a", "cordex.adminchat", "ac"))
        proxy.pluginManager.registerCommand(this, BanCommand(this, "ban", "cordex.ban", "ipban"))
        proxy.pluginManager.registerCommand(this, UnbanCommand(this, "unban", "cordex.ban", "unbanip"))
        proxy.pluginManager.registerCommand(this, KickCommand(this, "kick", "cordex.kick"))
        proxy.pluginManager.registerCommand(this, WarnCommand(this, "warn", "cordex.warn"))
        proxy.pluginManager.registerCommand(this, MuteCommand(this, "mute", "cordex.mute"))
        proxy.pluginManager.registerCommand(this, UnmuteCommand(this, "unmute", "cordex.mute"))
        proxy.pluginManager.registerCommand(this, FindCommand(this, "find", "default.global"))
        proxy.pluginManager.registerCommand(this, ProxyCommand(this, "proxy", "cordex.proxy"))
        proxy.pluginManager.registerCommand(this, WhoIsCommand(this, "whois", "cordex.whois"))
        proxy.pluginManager.registerCommand(this, ReportCommand(this, "report", "default.global"))
        proxy.pluginManager.registerCommand(this, BotProtectCommand(this, "botprot", "trainee.global"))
    }

    override fun onDisable() {
        playerController.onDisable()
        databaseController.disconnect()
    }
}
