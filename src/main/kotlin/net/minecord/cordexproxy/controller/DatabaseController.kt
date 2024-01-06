package net.minecord.cordexproxy.controller

import net.minecord.cordexproxy.CordexProxy
import net.minecord.cordexproxy.model.controller.ban.BanStorage
import net.minecord.cordexproxy.model.controller.config.ConfigValue
import net.minecord.cordexproxy.model.controller.player.IpStorage
import net.minecord.cordexproxy.model.controller.chat.MotdStorage
import net.minecord.cordexproxy.model.controller.chat.MotdType
import net.minecord.cordexproxy.model.controller.database.DatabaseCredentials
import net.minecord.cordexproxy.model.controller.database.MySQL
import net.minecord.cordexproxy.model.controller.log.LogType
import net.minecord.cordexproxy.model.controller.player.CordPlayer
import net.minecord.cordexproxy.model.controller.player.PlayerStorage
import net.minecord.cordexproxy.model.controller.server.ServerStorage
import net.minecord.cordexproxy.model.controller.translation.LanguageType
import net.minecord.cordexproxy.model.controller.translation.TranslationStorage
import java.sql.SQLException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class DatabaseController(cordexProxy: CordexProxy, credentials: DatabaseCredentials) : BaseController(cordexProxy) {
    private val mysql: MySQL = MySQL(credentials.host, credentials.port, credentials.name, credentials.user, credentials.pass, cordexProxy)

    init {
        mysql.createConnection()

        if (mysql.isConnected)
            cordexProxy.logController.log("DatabaseController &b| &7Successfully &aconnected &7to MariaDB", LogType.INFO)
        else {
            cordexProxy.logController.log("DatabaseController &b| &7Connection &cfailed", LogType.INFO)
        }
    }

    fun disconnect() {
        mysql.close()
    }

    /**
     * Caches prefixes from database to memory
     */
    internal fun loadMotds(): HashMap<LanguageType, HashMap<MotdType, ArrayList<MotdStorage>>> {
        val mainList = HashMap<LanguageType, HashMap<MotdType, ArrayList<MotdStorage>>>()

        try {
            val rs = mysql.query("SELECT * FROM `minecraft_motd` WHERE `is_enabled` = 1")!!.resultSet
            while (rs.next()) {
                val languageType = LanguageType.valueOf(rs.getString("language").toUpperCase())
                val type = MotdType.valueOf(rs.getString("type").toUpperCase())

                if (!mainList.containsKey(languageType))
                    mainList[languageType] = HashMap()

                val motds = mainList[languageType]
                if (motds != null) {
                    if (!motds.containsKey(MotdType.REFRESH))
                        motds[MotdType.REFRESH] = ArrayList()
                    if (!motds.containsKey(MotdType.TABLIST))
                        motds[MotdType.TABLIST] = ArrayList()
                    if (!motds.containsKey(MotdType.FAKAHEDA))
                        motds[MotdType.FAKAHEDA] = ArrayList()

                    val refreshMotds = motds[MotdType.REFRESH]
                    val tablistMotds = motds[MotdType.TABLIST]
                    val fakahedaMotds = motds[MotdType.FAKAHEDA]

                    val motd = MotdStorage(type, rs.getString("first_payload"), rs.getBoolean("is_first_centered"), rs.getString("second_payload"), rs.getBoolean("is_second_centered"), LanguageType.valueOf(rs.getString("language").toUpperCase()))

                    if (type == MotdType.REFRESH && refreshMotds != null) {
                        refreshMotds.add(motd)
                        motds[MotdType.REFRESH] = refreshMotds
                    }

                    if (type == MotdType.TABLIST && tablistMotds != null) {
                        tablistMotds.add(motd)
                        motds[MotdType.TABLIST] = tablistMotds
                    }

                    if (type == MotdType.FAKAHEDA && fakahedaMotds != null) {
                        fakahedaMotds.add(motd)
                        motds[MotdType.FAKAHEDA] = fakahedaMotds
                    }

                    mainList[languageType] = motds
                }
            }
            rs.close()

        } catch (e: SQLException) {
            e.printStackTrace()
        }

        return mainList
    }

    /**
     * Caches translations from database to memory
     */
    internal fun loadTranslations(): List<TranslationStorage> {
        val translations = ArrayList<TranslationStorage>()
        try {
            val rs = mysql.query("SELECT * FROM `minecraft_message`")!!.resultSet
            while (rs.next())
                translations.add(TranslationStorage(rs.getString("string"), rs.getString("content"),
                        LanguageType.valueOf(rs.getString("language").toUpperCase()), rs.getBoolean("is_enabled")))
            rs.close()
        } catch (e: SQLException) {
            e.printStackTrace()
        }

        return translations
    }

    /**
     * Caches translations from database to memory
     */
    internal fun loadBans(): List<BanStorage> {
        val bans = ArrayList<BanStorage>()
        try {
            val rs = mysql.query("SELECT b.*, p.name FROM `minecraft_ban` b JOIN `minecraft_player` p ON b.target_id = p.id WHERE b.`is_active`='1'")!!.resultSet
            while (rs.next())
                bans.add(BanStorage(rs.getInt("id"), rs.getInt("target_id"), rs.getString("name"), rs.getInt("target_ip"), rs.getInt("admin_id"), rs.getInt("admin_ip"),
                        rs.getString("reason"), rs.getTimestamp("expire_at"), rs.getBoolean("is_ipban"), rs.getBoolean("is_active")))
            rs.close()
        } catch (e: SQLException) {
            e.printStackTrace()
        }

        return bans
    }

    /**
     * Caches translations from database to memory
     */
    internal fun loadBanedNicknames(): ArrayList<String> {
        val nicknames= arrayListOf<String>()

        try {
            val rs = mysql.query("SELECT * FROM `minecraft_ban` b JOIN `minecraft_player` p ON b.target_id = p.id WHERE b.`is_active`='1'")!!.resultSet
            while (rs.next())
                nicknames.add(rs.getString("name"))
            rs.close()
        } catch (e: SQLException) {
            e.printStackTrace()
        }

        return nicknames
    }

    /**
     * Caches servers from database to memory
     */
    internal fun loadServers(): List<ServerStorage> {
        val servers = ArrayList<ServerStorage>()
        try {
            val rs = mysql.query("SELECT * FROM `minecraft_server`")!!.resultSet
            while (rs.next())
                servers.add(ServerStorage(rs.getInt("id"), rs.getString("name"), rs.getString("display_name"), rs.getString("game_type"),
                        rs.getInt("players"), rs.getInt("max_players"), rs.getInt("ram_max"), rs.getInt("ram_usage"), rs.getFloat("tps"), rs.getTimestamp("last_start"), rs.getTimestamp("last_stop")))
            rs.close()
        } catch (e: SQLException) {
            e.printStackTrace()
        }

        return servers
    }

    /**
     * Caches prefixes from database to memory
     */
    internal fun loadPrefixes(): HashMap<String, String> {
        val prefixes = HashMap<String, String>()
        try {
            val rs = mysql.query("SELECT * FROM `minecraft_prefix`")!!.resultSet
            while (rs.next())
                prefixes[rs.getString("name")] = rs.getString("payload")
            rs.close()
        } catch (e: SQLException) {
            e.printStackTrace()
        }

        return prefixes
    }

    /**
     * Loads the player data according to UUID
     *
     * @return The player data
     */
    internal fun loadPlayerData(uuid: UUID): PlayerStorage? {
        val placeholders = ArrayList(listOf(uuid.toString()))
        val result = mysql.preparedQuery("SELECT mc_player.*, ip_add_first.ip first_ip_address, ip_add_last.ip last_ip_address, lucky_perms.primary_group FROM `minecraft_player` mc_player " +
                "INNER JOIN `ip_address` ip_add_first " +
                "ON ip_add_first.id = mc_player.first_ip_address_id " +
                "INNER JOIN `ip_address` ip_add_last " +
                "ON ip_add_last.id = mc_player.last_ip_address_id " +
                "LEFT JOIN `luckperms_players` lucky_perms " +
                "ON lucky_perms.uuid = mc_player.uuid " +
                "WHERE mc_player.uuid = ?", placeholders)

        return retrievePlayerData(result)
    }

    internal fun loadPlayerData(name: String): PlayerStorage? {
        val placeholders = listOf(name, name, name)
        val result = mysql.preparedQuery("SELECT mc_player.*, ip_add_first.ip first_ip_address, ip_add_last.ip last_ip_address, lucky_perms.primary_group FROM " +
                "((SELECT * FROM `minecraft_player` first_search WHERE `name` = ? AND `type` = 'online' ORDER BY last_login DESC LIMIT 1) UNION " +
                "(SELECT * FROM `minecraft_player` second_search WHERE `name` = ? AND `type` = 'warez' ORDER BY last_login DESC LIMIT 1) UNION " +
                "(SELECT * FROM `minecraft_player` third_search WHERE `name` = ? AND `type` = 'offline' ORDER BY last_login DESC LIMIT 1)) mc_player " +
                "INNER JOIN `ip_address` ip_add_first " +
                "ON ip_add_first.id = mc_player.first_ip_address_id " +
                "INNER JOIN `ip_address` ip_add_last " +
                "ON ip_add_last.id = mc_player.last_ip_address_id " +
                "LEFT JOIN `luckperms_players` lucky_perms " +
                "ON lucky_perms.uuid = mc_player.uuid " +
                "ORDER BY mc_player.last_login DESC LIMIT 1", placeholders)

        return retrievePlayerData(result)
    }

    private fun retrievePlayerData(result: MySQL.Result?): PlayerStorage? {
        var playerData: PlayerStorage? = null

        if (result != null) {
            val rs = result.resultSet
            try {
                if (rs.next()) {
                    playerData = PlayerStorage(rs.getInt("id"), rs.getString("name"), UUID.fromString(rs.getString("uuid")), rs.getBoolean("is_online"), rs.getBoolean("is_logged"), rs.getString("type"), rs.getBoolean("is_whitelisted"),
                            rs.getInt("first_ip_address_id"), rs.getString("first_ip_address"), rs.getInt("last_ip_address_id"), rs.getString("last_ip_address"), rs.getInt("played_time"), rs.getTimestamp("first_join"), rs.getTimestamp("first_login"),
                            rs.getTimestamp("last_join"), rs.getTimestamp("last_login"), cordexProxy.rankController.getRank(
                            rs.getString("primary_group") ?: "default"
                        ))
                }
                rs.close()
            } catch (e: SQLException) {
                e.printStackTrace()
            }

        }

        return playerData
    }

    fun loadIpData(ip: String): IpStorage {
        val placeholders = ArrayList(listOf(ip))
        val ipStorage = IpStorage(0, "US", "USD", "cs")
        try {
            val rs = mysql.preparedQuery("SELECT * FROM `ip_address` WHERE `ip` = ?", placeholders)!!.resultSet
            if (rs.next()) {
                ipStorage.id = rs.getInt("id")
                ipStorage.country = rs.getString("country")
                ipStorage.currency = rs.getString("currency")
                ipStorage.setLanguage(rs.getString("language"))
            }
            rs.close()

            if (ipStorage.id != 0) {
                return ipStorage
            }

            val rs2 = mysql.preparedQuery("SELECT ip_locations.*, ip4_blocks.is_anonymous_proxy FROM ip4_blocks JOIN ip_locations ON ip4_blocks.geoname_id = ip_locations.geoname_id WHERE INET_ATON(?) BETWEEN ip4_blocks.ip_from AND ip4_blocks.ip_to LIMIT 1", placeholders)!!.resultSet
            if (rs2.next()) {
                val countryIso = rs2.getString("country_iso_code")
                val language = if (countryIso == "SK") "sk" else if (countryIso == "CZ") "cs" else "en"
                ipStorage.country = countryIso
                ipStorage.currency = if (countryIso == "SK") "EUR" else if (countryIso == "CZ") "CZK" else "EUR"
                ipStorage.setLanguage(language)

                val placeholders2 = arrayListOf(ip, ipStorage.country, ipStorage.currency, language)
                ipStorage.id = mysql.getInsertedRow("INSERT INTO `ip_address` (`ip`, `country`, `currency`, `language`) VALUES (?, ?, ?, ?)", placeholders2)
            } else {
                ipStorage.id = 0
                ipStorage.country = "UNKNOWN"
                ipStorage.currency = "EUR"
                ipStorage.setLanguage("cs")
            }
            rs2.close()

        } catch (ex: SQLException) {
            ex.printStackTrace()
        }

        return ipStorage
    }

    fun getBanCount(playerId: Int): Int {
        val placeholders = ArrayList(listOf(playerId.toString()))
        var banCount = 0

        try {
            val rs = mysql.preparedQuery("SELECT COUNT(id) as ban_count FROM `minecraft_ban` WHERE target_id = ?", placeholders)!!.resultSet
            if (rs.next())
                banCount = rs.getInt("ban_count")
            rs.close()
        } catch (e: SQLException) {
            e.printStackTrace()
        }

        return banCount
    }

    fun getReportCount(playerId: Int): Int {
        val placeholders = ArrayList(listOf(playerId.toString()))
        var reportCount = 0

        try {
            val rs = mysql.preparedQuery("SELECT COUNT(id) as report_count FROM `minecraft_report` WHERE target_id = ?", placeholders)!!.resultSet
            if (rs.next())
                reportCount = rs.getInt("report_count")
            rs.close()
        } catch (e: SQLException) {
            e.printStackTrace()
        }

        return reportCount
    }

    fun getReportCountInLastTenMinutes(playerId: Int): Int {
        val placeholders = ArrayList(listOf(playerId.toString()))
        var reportCount = 0

        try {
            val rs = mysql.preparedQuery("SELECT COUNT(id) as report_count FROM `minecraft_report` WHERE target_id = ? AND created_at > NOW() - INTERVAL 10 MINUTE", placeholders)!!.resultSet
            if (rs.next())
                reportCount = rs.getInt("report_count")
            rs.close()
        } catch (e: SQLException) {
            e.printStackTrace()
        }

        return reportCount
    }

    /**
     * Finds out if player is able to report some player
     *
     * @return False if last report was before less than 10 mins
     */
    fun isAbleToReportPlayer(playerId: Int): Boolean {
        val placeholders = ArrayList(listOf(playerId.toString() + ""))
        var isAble = true

        try {
            val rs = mysql.preparedQuery("SELECT * FROM `minecraft_report` WHERE reporter_id = ? AND created_at > date_sub(now(), interval 10 minute)", placeholders)!!.resultSet
            if (rs.next())
                isAble = false
            rs.close()
        } catch (e: SQLException) {
            e.printStackTrace()
        }

        return isAble
    }

    /**
     * Reports the player with reason
     */
    fun reportPlayer(targetId: Int, reporterId: Int, reason: String) {
        val placeholders = ArrayList(listOf(targetId.toString() + "", reporterId.toString() + "", reason))

        mysql.preparedQuery("INSERT INTO `minecraft_report` (`target_id`, `reporter_id`, `reason`) VALUES (?, ?, ?)", placeholders)
    }

    /**
     * Caches config values from database to memory
     *
     * @return Config values
     */
    internal fun loadConfigValues(): ConcurrentHashMap<String, ArrayList<ConfigValue>> {
        val output = ConcurrentHashMap<String, ArrayList<ConfigValue>>()

        try {
            val rs = mysql.query("SELECT `conf_value`, `conf_key` FROM `minecraft_config`")!!.resultSet
            while (rs.next()) {
                val key = rs.getString("conf_key")
                if (!output.containsKey(key))
                    output[key] = ArrayList()
                output[key]!!.add(ConfigValue(rs.getString("conf_value")))
            }
            rs.close()
        } catch (e: SQLException) {
            e.printStackTrace()
        }

        return output
    }

    fun setConfigValue(key: String, value: String) {
        val placeholders = listOf(value, key)

        mysql.preparedQuery("UPDATE `minecraft_config` SET `conf_value` = ? WHERE `conf_key` = ?", placeholders)
    }

    fun getInfo(infoKey: String): Int {
        val placeholders = listOf(infoKey)
        var output = 0

        try {
            val rs = mysql.preparedQuery("SELECT `info_value` FROM `minecraft_info` WHERE `info_key` = ?", placeholders)!!.resultSet
            while (rs.next())
                output = rs.getInt("info_value")
            rs.close()
        } catch (e: SQLException) {
            e.printStackTrace()
        }

        return output
    }

    fun getServerlistId(name: String): Int {
        val placeholders = listOf(name)
        var output = 0

        try {
            val rs = mysql.preparedQuery("SELECT `id` FROM `minecraft_serverlist` WHERE `votifier_name` = ?", placeholders)!!.resultSet
            while (rs.next())
                output = rs.getInt("id")
            rs.close()
        } catch (e: SQLException) {
            e.printStackTrace()
        }

        return output
    }

    fun setInfo(value: Int, key: String) {
        val placeholders = listOf(value.toString(), key)

        mysql.preparedQuery("UPDATE `minecraft_info` SET `info_value` = ? WHERE `info_key` = ?", placeholders)
    }

    fun isLogged(playerId: Int): Boolean {
        val placeholders = listOf(playerId.toString())
        var output = false

        try {
            val rs = mysql.preparedQuery("SELECT `is_logged` FROM `minecraft_player` WHERE `id` = ?", placeholders)!!.resultSet
            while (rs.next())
                output = rs.getBoolean("is_logged")
            rs.close()
        } catch (e: SQLException) {
            e.printStackTrace()
        }

        return output
    }

    fun loadBan(targetId: Int, targetIp: Int): BanStorage? {
        val placeholders = listOf(targetId.toString(), targetIp.toString())
        var output: BanStorage? = null

        try {
            val rs = mysql.preparedQuery("SELECT b.*, p.name FROM `minecraft_ban` b JOIN `minecraft_player` p ON b.target_id = p.id WHERE b.`is_active` = 1 AND (b.`target_id` = ? OR b.`target_ip` = ?)", placeholders)!!.resultSet
            if (rs.next())
                output = BanStorage(rs.getInt("id"), rs.getInt("target_id"), rs.getString("name"), rs.getInt("target_ip"), rs.getInt("admin_id"), rs.getInt("admin_ip"),
                        rs.getString("reason"), rs.getTimestamp("expire_at"), rs.getBoolean("is_ipban"), rs.getBoolean("is_active"))
            rs.close()
        } catch (e: SQLException) {
            e.printStackTrace()
        }

        return output
    }

    fun removeBan(targetId: Int, targetIp: Int) {
        val placeholders = listOf(targetId.toString(), targetIp.toString())

        mysql.preparedQuery("UPDATE `minecraft_ban` SET `is_active` = 0 WHERE `target_id` = ? OR `target_ip` = ?", placeholders)
    }

    fun insertPlayerData(playerStorage: PlayerStorage) {
        val ipAddressId = cordexProxy.cacheController.getIpData(playerStorage.firstIpAddress).id
        val placeholders = listOf(playerStorage.name, playerStorage.uuid.toString(), if (playerStorage.isOnline) "1" else "0", playerStorage.type, ipAddressId.toString(), ipAddressId.toString())

        mysql.preparedQuery("INSERT INTO `minecraft_player` (`name`, `uuid`, `is_online`, `type`, `first_ip_address_id`, `last_ip_address_id`) VALUES (?, ?, ?, ?, ?, ?)", placeholders)
    }

    fun insertBan(ban: BanStorage) {
        val placeholders = listOf(ban.adminId.toString(), ban.adminIp.toString(), ban.targetId.toString(), ban.targetIp.toString(), ban.reason, if (ban.isIpBan) "1" else "0", ban.expire.toString())

        mysql.preparedQuery("INSERT INTO `minecraft_ban` (`admin_id`, `admin_ip`, `target_id`, `target_ip`, `reason`, `is_ipban`, `expire_at`) VALUES (?, ?, ?, ?, ?, ?, ?)", placeholders)
    }

    fun insertVote(playerId: Int, serverListId: Int) {
        val utcDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        utcDate.timeZone = TimeZone.getTimeZone("UTC")
        val placeholders = listOf(playerId.toString(), serverListId.toString(), utcDate.format(Date()))

        mysql.preparedQuery("INSERT INTO `minecraft_vote` (`player_id`, `serverlist_id`, `created_at`) VALUES (?, ?, ?)", placeholders)
    }

    fun insertDelivery(playerId: Int, itemId: Int, amount: Int, serverType: String) {
        val utcDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        utcDate.timeZone = TimeZone.getTimeZone("UTC")
        val placeholders = listOf(playerId.toString(), itemId.toString(), amount.toString(), serverType, utcDate.format(Date()))

        mysql.preparedQuery("INSERT INTO `minecraft_player_delivery` (`player_id`, `item_id`, `amount`, `server_type`, `created_at`) VALUES (?, ?, ?, ?, ?)", placeholders)
    }

    fun updatePlayerData(playerStorage: PlayerStorage) {
        val ipAddressId = cordexProxy.cacheController.getIpData(playerStorage.lastIpAddress).id
        val placeholders = listOf(playerStorage.name, if (playerStorage.isOnline) "1" else "0", playerStorage.type, ipAddressId.toString(), playerStorage.uuid.toString())

        if (playerStorage.firstIp == 0) {
            mysql.preparedQuery("UPDATE `minecraft_player` SET `name` = ?, `is_online` = ?, `type` = ?, `last_ip_address_id` = ? WHERE `uuid` = ?", placeholders)
            mysql.preparedQuery("UPDATE `minecraft_player` SET `name` = ?, `is_online` = ?, `type` = ?, `first_ip_address_id` = ? WHERE `uuid` = ?", placeholders)
        } else {
            mysql.preparedQuery("UPDATE `minecraft_player` SET `name` = ?, `is_online` = ?, `type` = ?, `last_ip_address_id` = ? WHERE `uuid` = ?", placeholders)
        }
    }

    fun onQuit(uuid: UUID) {
        val placeholders = ArrayList(listOf(uuid.toString()))

        mysql.preparedQuery("UPDATE `minecraft_player` SET `is_online` = 0, `is_logged` = 0 WHERE `uuid` = ?", placeholders)
    }

    fun failSafe() {
        mysql.query("UPDATE `minecraft_player` SET `is_online` = 0, `is_logged` = 0")
    }

    fun updateQuitInfo(cordPlayer: CordPlayer) {
        val placeholders = listOf(((System.currentTimeMillis() - cordPlayer.data.connectTime!!.time) / 1000).toString(), cordPlayer.data.id.toString())

        mysql.preparedQuery("UPDATE `minecraft_player` SET `played_time` = (? + `played_time` - `afk_time`), `afk_to` = 0, `afk_since` = 0, `afk_time` = 0 WHERE `id` = ? AND `is_afk` = 0", placeholders)
    }

    fun updateLastJoin(cordPlayer: CordPlayer) {
        val placeholders = ArrayList(listOf(cordPlayer.data.id.toString()))

        mysql.preparedQuery("UPDATE `minecraft_player` SET `last_join` = CURRENT_TIMESTAMP, `is_afk` = 0, `afk_time` = 0, `afk_since` = 0, `afk_to` = 0 WHERE `id` = ?", placeholders)
    }
}
