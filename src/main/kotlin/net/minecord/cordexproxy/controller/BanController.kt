package net.minecord.cordexproxy.controller

import net.minecord.cordexproxy.CordexProxy
import net.minecord.cordexproxy.model.controller.ban.MuteStorage
import net.minecord.cordexproxy.model.controller.player.BungeeTitle
import net.minecord.cordexproxy.model.controller.player.CordPlayer
import net.minecord.cordexproxy.model.controller.player.PlayerStorage
import net.minecord.cordexproxy.model.controller.ban.BanStorage
import net.md_5.bungee.api.chat.TextComponent
import net.minecord.cordexproxy.util.colored
import net.minecord.cordexproxy.util.formatTime
import java.lang.NullPointerException
import java.sql.Timestamp
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class BanController(cordexProxy: CordexProxy) : BaseController(cordexProxy) {
    private val mutedPlayers = ConcurrentHashMap<UUID, MuteStorage>()

    init {
        cordexProxy.proxy.scheduler.schedule(cordexProxy, {
            mutedPlayers.forEach { (uuid, muteStorage) ->
                val ms = muteStorage.expireAt!!.time - System.currentTimeMillis()

                if (ms <= 0) {
                    mutedPlayers.remove(uuid)
                } else {
                    try {
                        val cordPlayer = cordexProxy.playerController.getPlayerByUniqueId(muteStorage.uuid!!)
                        cordPlayer.sendActionBar(cordPlayer.translateMessage("muteReminder").replace("%time%", ms.toInt().formatTime()).colored())

                    } catch (e: NullPointerException) { }
                }
            }
        }, 0, 1, TimeUnit.SECONDS)

        cordexProxy.proxy.scheduler.schedule(cordexProxy, {
            cordexProxy.cacheController.bans.forEach { ban ->
                if (ban.expire.time <= System.currentTimeMillis()) {
                    cordexProxy.cacheController.removeBanData(ban.targetId, ban.targetIp)
                    removeBan(ban.targetId, ban.targetIp)
                }
            }
        }, 0, 60, TimeUnit.SECONDS)
    }

    fun insertBan(admin: CordPlayer?, target: PlayerStorage, reason: String, seconds: Int, ipBan: Boolean) {
        val placeholders = HashMap<String, String>()
        val hasBan = cordexProxy.databaseController.loadBan(target.id, target.lastIp)

        if (hasBan != null && admin != null) {
            admin.sendMessage("banlist", "%prefix% Player or his IP address is already banned!")
            return
        }

        if (target.lastIp == 0) {
            if (admin != null) {
                admin.sendMessage("banlist", "%prefix% Corrupted IP address")
                return
            }
        }

        var bannedPlayer: CordPlayer? = null
        try {
            bannedPlayer = cordexProxy.playerController.getPlayerByUniqueId(target.uuid)
            placeholders["%tcolor%"] = bannedPlayer.rank.chatColor.toString() + ""
            val bannedTitle = BungeeTitle()
            bannedTitle.title(*TextComponent.fromLegacyText(bannedPlayer.translateMessage("bannedTitle").colored()))
            bannedTitle.subTitle(*TextComponent.fromLegacyText(bannedPlayer.translateMessage("bannedSubTitle").replace("%reason%", reason).colored()))
            bannedTitle.stay(60)
            bannedTitle.send(bannedPlayer.player)
        } catch (e: NullPointerException) {
            placeholders["%tcolor%"] = "&9"
        }

        var adminId = 0
        var adminIp = 0
        if (admin != null) {
            adminId = admin.data.id
            adminIp = admin.data.lastIp
        }

        val current = Timestamp(System.currentTimeMillis())

        val cal = Calendar.getInstance()
        cal.timeInMillis = current.time
        cal.add(Calendar.SECOND, seconds)

        val expire = Timestamp(cal.time.time)

        val ban = BanStorage(0, target.id, target.name, target.lastIp, adminId, adminIp, reason, expire, ipBan, true)

        cordexProxy.cacheController.cacheBanData(ban)
        cordexProxy.databaseController.insertBan(ban)

        cordexProxy.proxy.scheduler.schedule(cordexProxy, {
            placeholders["%target%"] = target.name
            placeholders["%reason%"] = reason
            cordexProxy.translationController.broadcastRandomTranslate("banBroadcast", "banlist", placeholders)
            if (bannedPlayer != null) {
                val ipStorage = cordexProxy.databaseController.loadIpData(bannedPlayer.data.lastIpAddress)

                var text = cordexProxy.translationController.getTranslation(bannedPlayer.language, "bannedDisconnect").colored().replace("%reason%", ban.reason).replace("%expire%", ban.getFriendlyLeftTime()).replace("\\n", "\n")

                text = text.replace("%country%", ipStorage.country)
                text = text.replace("%nick%", bannedPlayer.player.name)

                if (ipStorage.country == "CZ") {
                    text = text.replace("%number%", "90733149")
                    text = text.replace("%price%", "149 Kc")
                } else if (ipStorage.country == "SK") {
                    text = text.replace("%number%", "88770600")
                    text = text.replace("%price%", "6,00 €")
                }

                bannedPlayer.player.disconnect(*TextComponent.fromLegacyText(text))
            }
        }, 3, TimeUnit.SECONDS)
    }

    fun removeBan(targetId: Int, targetIp: Int) {
        cordexProxy.databaseController.removeBan(targetId, targetIp)
    }

    fun mutePlayer(target: CordPlayer, reason: String, seconds: Int) {
        val placeholders = HashMap<String, String>()
        val expire = Timestamp(System.currentTimeMillis() + seconds * 1000)
        val networkName = target.translateMessage("serverName")

        placeholders["%tcolor%"] = target.rank.chatColor.toString() + ""
        placeholders["%target%"] = target.data.name
        placeholders["%reason%"] = reason

        cordexProxy.translationController.broadcastRandomTranslate("muteBroadcast", "banlist", placeholders)
        mutedPlayers[target.data.uuid] = MuteStorage(target.data.id, target.player.uniqueId, target.data.name, reason, Timestamp(System.currentTimeMillis()), expire)

        val title = BungeeTitle()
        title.title(*TextComponent.fromLegacyText(target.translateMessage("mutedTitle").colored()))
        title.subTitle(*TextComponent.fromLegacyText(target.translateMessage("bannedSubTitle").replace("%reason%", reason).replace("%network%", networkName).colored()))
        title.stay(60)
        title.send(target.player)
    }

    fun isMuted(cordPlayer: CordPlayer): Boolean {
        return mutedPlayers.containsKey(cordPlayer.data.uuid)
    }

    fun kickPlayer(target: CordPlayer, reason: String) {
        cordexProxy.translationController.broadcastRandomTranslate("kickBroadcast", "banlist", createPlaceholders(target, reason))

        target.player.disconnect(*TextComponent.fromLegacyText(target.translateMessage("kickedDisconnect").replace("%reason%", reason).colored().replace("\\n", "\n").replace("%network%", target.translateMessage("serverName"))))
    }

    fun warnPlayer(target: CordPlayer, reason: String) {
        cordexProxy.translationController.broadcastRandomTranslate("warnBroadcast", "banlist", createPlaceholders(target, reason))

        val title = BungeeTitle()
        title.title(*TextComponent.fromLegacyText(target.translateMessage("warnedTitle").colored()))
        title.subTitle(*TextComponent.fromLegacyText(target.translateMessage("bannedSubTitle").replace("%reason%", reason).replace("%network%", target.translateMessage("serverName")).colored()))
        title.stay(60)
        title.send(target.player)
    }

    private fun createPlaceholders(target: CordPlayer, reason: String): HashMap<String, String> {
        val placeholders = hashMapOf<String, String>()

        placeholders["%tcolor%"] = target.rank.chatColor.toString() + ""
        placeholders["%target%"] = target.data.name
        placeholders["%reason%"] = reason

        return placeholders
    }
}
