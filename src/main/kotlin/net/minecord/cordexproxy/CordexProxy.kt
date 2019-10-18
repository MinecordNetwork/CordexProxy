package net.minecord.cordexproxy

import net.minecord.cordexproxy.command.*
import net.minecord.cordexproxy.controller.*
import net.minecord.cordexproxy.listener.*
import net.minecord.cordexproxy.model.controller.database.DatabaseCredentials
import net.minecord.cordexproxy.model.controller.log.LogType
import net.md_5.bungee.api.plugin.Plugin
import net.md_5.bungee.config.Configuration
import net.md_5.bungee.config.ConfigurationProvider
import net.md_5.bungee.config.YamlConfiguration

import java.io.File
import java.io.IOException
import java.nio.file.Files

class CordexProxy : Plugin() {
    lateinit var logController: LogController
        private set
    lateinit var chatController: ChatController
        private set
    lateinit var databaseController: DatabaseController
        private set
    lateinit var translationController: TranslationController
        private set
    lateinit var rankController: RankController
        private set
    lateinit var playerController: PlayerController
        private set
    lateinit var utilController: UtilController
        private set
    lateinit var cacheController: CacheController
        private set
    lateinit var serverController: ServerController
        private set
    lateinit var banController: BanController
        private set

    override fun onEnable() {
        if (!dataFolder.exists())
            dataFolder.mkdir()

        val file = File(dataFolder, "config.yml")

        if (!file.exists()) {
            try {
                getResourceAsStream("config.yml").use { `in` -> Files.copy(`in`, file.toPath()) }
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }

        var cfg: Configuration? = null
        try {
            cfg = ConfigurationProvider.getProvider(YamlConfiguration::class.java).load(File(dataFolder, "config.yml"))
        } catch (e: IOException) {
            e.printStackTrace()
        }

        assert(cfg != null)

        logController = LogController(this)
        if (cfg!!.getString("database.name") == "minecraft" && cfg.getString("database.pass") == "password") {
            logController.log("Database is not configured!", LogType.ERROR)
            logController.log("Plugin disabled!", LogType.WARNING)
            return
        }

        proxy.registerChannel("ProxyPrivateChannel")

        databaseController = DatabaseController(this, DatabaseCredentials(cfg.getString("database.host"), cfg.getInt("database.port"), cfg.getString("database.name"), cfg.getString("database.user"), cfg.getString("database.pass")))
        cacheController = CacheController(this)
        chatController = ChatController(this)
        translationController = TranslationController(this)
        serverController = ServerController(this)
        rankController = RankController(this)
        playerController = PlayerController(this)
        utilController = UtilController(this)
        banController = BanController(this)

        proxy.pluginManager.registerListener(this, PlayerListener(this))
        proxy.pluginManager.registerListener(this, GatewayListener(this))
        proxy.pluginManager.registerListener(this, PingListener(this))
        proxy.pluginManager.registerListener(this, SecurityListener(this))
        proxy.pluginManager.registerListener(this, ConnectionListener(this))
        proxy.pluginManager.registerListener(this, VoteListener(this))

        proxy.pluginManager.registerCommand(this, AdminChatCommand(this, "a", "cordex.adminchat", "ac"))
        proxy.pluginManager.registerCommand(this, BanCommand(this, "ban", "cordex.ban", "ipban"))
        proxy.pluginManager.registerCommand(this, UnbanCommand(this, "unban", "cordex.ban", "unbanip"))
        proxy.pluginManager.registerCommand(this, KickCommand(this, "kick", "cordex.kick"))
        proxy.pluginManager.registerCommand(this, WarnCommand(this, "warn", "cordex.warn"))
        proxy.pluginManager.registerCommand(this, MuteCommand(this, "mute", "cordex.mute"))
        proxy.pluginManager.registerCommand(this, FindCommand(this, "find", "default.global"))
        proxy.pluginManager.registerCommand(this, ProxyCommand(this, "proxy", "cordex.proxy"))
        proxy.pluginManager.registerCommand(this, WhoIsCommand(this, "whois", "cordex.whois"))
    }

    override fun onDisable() {
        playerController.onDisable()
        databaseController.disconnect()
    }
}
