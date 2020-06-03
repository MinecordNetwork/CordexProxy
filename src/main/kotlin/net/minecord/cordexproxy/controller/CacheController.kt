package net.minecord.cordexproxy.controller

import net.minecord.cordexproxy.CordexProxy
import net.minecord.cordexproxy.model.controller.ban.BanStorage
import net.minecord.cordexproxy.model.controller.config.ConfigValue
import net.minecord.cordexproxy.model.controller.player.IpStorage
import net.minecord.cordexproxy.model.controller.player.PlayerStorage
import java.lang.NullPointerException

import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class CacheController(cordexProxy: CordexProxy) : BaseController(cordexProxy) {
    val bannedNicknames by lazy {
        cordexProxy.databaseController.loadBanedNicknames()
    }
    private val ipCache = ConcurrentHashMap<String, IpStorage>()
    private val banIpCache = ConcurrentHashMap<Int, BanStorage>()
    private val banPlayerCache = ConcurrentHashMap<Int, BanStorage>()
    private val playerCache = ConcurrentHashMap<UUID, PlayerStorage>()
    private val playerNameCache = ConcurrentHashMap<String, PlayerStorage>()
    private val cleaner = ConcurrentHashMap<String, Date>()
    private var configuration: ConcurrentHashMap<String, ArrayList<ConfigValue>>? = null
    private var playerRecord: Int = 0

    val bans: Collection<BanStorage>
        get() = banPlayerCache.values

    init {
        playerRecord = cordexProxy.databaseController.getInfo("online_players_record")
        configuration = cordexProxy.databaseController.loadConfigValues()

        for (banStorage in cordexProxy.databaseController.loadBans())
            cacheBanData(banStorage)

        cordexProxy.proxy.scheduler.schedule(cordexProxy, {
            cleaner.forEach { (ip, date) ->
                if (date.time / 1000 < System.currentTimeMillis() / 1000 - 7200) {
                    cleaner.remove(ip)
                    ipCache.remove(ip)
                }
            }
        }, 0, 60, TimeUnit.SECONDS)

        cordexProxy.proxy.scheduler.schedule(cordexProxy, { configuration = cordexProxy.databaseController.loadConfigValues() }, 60, 60, TimeUnit.SECONDS)

        cordexProxy.proxy.scheduler.schedule(cordexProxy, {
            playerCache.forEach { (uuid, playerStorage) ->
                if (playerStorage.connectTime!!.time < System.currentTimeMillis() - 16000) {
                    playerCache.remove(uuid)
                    playerNameCache.remove(playerStorage.name)
                }
            }
        }, 16, 16, TimeUnit.SECONDS)
    }

    fun debug(): List<String> {
        val list = ArrayList<String>()

        list.add(" Stored IP adresses: &e" + ipCache.size)
        list.add(" Stored banned IP adresses: &e" + banIpCache.size)
        list.add(" Stored banned players: &e" + banPlayerCache.size)
        list.add(" Stored player data objects to UUID: &e" + playerCache.size)
        list.add(" Stored player data objects to Name: &e" + playerNameCache.size)
        list.add(" Stored configuration values: &e" + configuration!!.size)

        return list
    }

    private fun cacheIpData(ip: String, ipStorage: IpStorage) {
        ipCache[ip] = ipStorage
        cleaner[ip] = Date()
    }

    fun getIpData(ip: String): IpStorage {
        if (!ipCache.containsKey(ip))
            cacheIpData(ip, cordexProxy.databaseController.loadIpData(ip))
        else
            cleaner[ip] = Date()

        return ipCache[ip] ?: throw NullPointerException()
    }

    fun getPlayerData(uuid: UUID): PlayerStorage? {
        if (!playerCache.containsKey(uuid)) {
            val playerStorage = cordexProxy.databaseController.loadPlayerData(uuid)
            if (playerStorage != null)
                cachePlayerData(playerStorage)
        }

        return playerCache[uuid]
    }

    fun getPlayerData(name: String): PlayerStorage? {
        if (!playerNameCache.containsKey(name)) {
            val playerStorage = cordexProxy.databaseController.loadPlayerData(name)
            if (playerStorage != null)
                cachePlayerData(playerStorage)
        }

        return playerNameCache[name]
    }

    private fun cachePlayerData(playerStorage: PlayerStorage) {
        playerCache[playerStorage.uuid] = playerStorage
        playerNameCache[playerStorage.name] = playerStorage
    }

    fun getConfigValue(key: String): ConfigValue {
        return configuration!![key]!![0]
    }

    fun setConfigValue(key: String, value: String) {
        cordexProxy.databaseController.setConfigValue(key, value)
        configuration!![key] = arrayListOf(ConfigValue(value))
    }

    fun cacheBanData(banStorage: BanStorage) {
        if (banStorage.isIpBan)
            banIpCache[banStorage.targetIp] = banStorage
        banPlayerCache[banStorage.targetId] = banStorage
    }

    fun removeBanData(playerId: Int, playerIp: Int, nickname: String? = null) {
        banIpCache.remove(playerIp)
        banPlayerCache.remove(playerId)
        if (nickname != null)
            bannedNicknames.remove(nickname)
    }

    fun getBanData(ipAddress: Int, playerId: Int?): BanStorage? {
        var banStorage = banIpCache[ipAddress]
        if (banStorage == null && playerId !== null)
            banStorage = banPlayerCache[playerId]
        return banStorage
    }

    fun getPlayerRecord(): Int {
        return playerRecord
    }

    fun setPlayerRecord(playerRecord: Int) {
        this.playerRecord = playerRecord
        cordexProxy.databaseController.setInfo(playerRecord, "online_players_record")
    }
}
