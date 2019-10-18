package net.minecord.cordexproxy.controller

import net.minecord.cordexproxy.CordexProxy
import net.minecord.cordexproxy.model.controller.chat.MotdType
import net.minecord.cordexproxy.model.controller.log.LogType
import net.minecord.cordexproxy.model.controller.player.CordPlayer
import net.minecord.cordexproxy.model.controller.translation.LanguageType
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.minecord.cordexproxy.model.controller.player.BungeeTitle
import net.minecord.cordexproxy.util.colored
import java.lang.NullPointerException

import java.sql.Timestamp
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.function.Consumer
import kotlin.math.roundToInt

class PlayerController(cordexProxy: CordexProxy) : BaseController(cordexProxy) {
    private val random = Random()
    private val players = HashMap<UUID, CordPlayer>()

    init {
        for (player in ProxyServer.getInstance().players) {
            addPlayer(player)

            val title = BungeeTitle()

            title.title(*TextComponent.fromLegacyText(""))
            title.subTitle(*TextComponent.fromLegacyText(getPlayer(player).translateMessage("enablingProxy").colored()))
            title.send(player)
        }

        keepTabListUpdated()
    }

    fun onDisable() {
        for (cordPlayer in getPlayers()) {
            val title = BungeeTitle()

            title.title(*TextComponent.fromLegacyText(""))
            title.subTitle(*TextComponent.fromLegacyText(cordPlayer.translateMessage("disablingProxy").colored()))
            title.send(cordPlayer.player)
        }
    }

    private fun keepTabListUpdated() {
        cordexProxy.proxy.scheduler.schedule(cordexProxy, { getPlayers().forEach(Consumer<CordPlayer> { this.showTablist(it) }) }, 0, 1, TimeUnit.SECONDS)
    }

    fun showTablist(cordPlayer: CordPlayer) {
        val motds = cordexProxy.chatController.getMotds(cordPlayer.language, MotdType.TABLIST)
        val motd = motds!![random.nextInt(motds.size)]

        val proxyServer = cordPlayer.player.server
        if (proxyServer == null || proxyServer.info == null || proxyServer.info.name == null)
            return

        val server = cordexProxy.serverController.getServer(proxyServer.info.name)

        val playerName = cordPlayer.player.name
        val serverName = server.displayName
        val serverTps = ((server.tps * 1000.0).roundToInt() / 1000.0).toString() + ""
        val serverPlayers = proxyServer.info.players.size
        val serverMaxPlayers = server.maxPlayers
        val ping = cordPlayer.player.ping
        val online = players.size
        val serverRam = server.ramUsage
        val serverRamMax = server.ramMax

        var playersWord = "player"
        if (cordPlayer.language == LanguageType.CS) {
            playersWord = "hrac"
            if (serverPlayers in 2..4)
                playersWord = "hraci"
            else if (serverPlayers >= 5)
                playersWord = "hracu"
        } else {
            if (serverPlayers > 1) {
                playersWord = "players"
            }
        }

        val networkName = cordPlayer.translateMessage("webName")

        val header = motd.firstPayload.replace("%players%", players.size.toString() + "")
                .replace("%serverName%", serverName).replace("%playerName%", playerName)
                .replace("%serverTps%", serverTps).replace("%serverPlayers%", serverPlayers.toString() + "")
                .replace("%ping%", ping.toString() + "").replace("%online%", online.toString() + "")
                .replace("%serverRam%", serverRam.toString() + "").replace("%serverRamMax%", serverRamMax.toString() + "")
                .replace("%serverMaxPlayers%", serverMaxPlayers.toString() + "").replace("%playersWord%", playersWord).replace("\\n", "\n")
                .replace("%network%", networkName).colored()

        val footer = motd.secondPayload.replace("%players%", players.size.toString() + "")
                .replace("%serverName%", serverName).replace("%playerName%", playerName)
                .replace("%serverTps%", serverTps).replace("%serverPlayers%", serverPlayers.toString() + "")
                .replace("%ping%", ping.toString() + "").replace("%online%", online.toString() + "")
                .replace("%serverRam%", serverRam.toString() + "").replace("%serverRamMax%", serverRamMax.toString() + "")
                .replace("%serverMaxPlayers%", serverMaxPlayers.toString() + "").replace("%playersWord%", playersWord).replace("\\n", "\n")
                .replace("%network%", networkName).colored()

        cordPlayer.player.setTabHeader(ComponentBuilder(header).create(), ComponentBuilder(footer).create())
    }

    /**
     * Adds player to cache
     *
     * @param player The bungee player
     */
    fun addPlayer(player: ProxiedPlayer) {
        val playerStorage = cordexProxy.cacheController.getPlayerData(player.uniqueId)
        playerStorage.connectTime = Timestamp(System.currentTimeMillis())

        val cordPlayer = CordPlayer(cordexProxy, player, playerStorage)

        players[player.uniqueId] = cordPlayer
        cordexProxy.databaseController.updateLastJoin(cordPlayer)

        cordexProxy.logController.log("&fPlayer " + cordPlayer.rank.chatColor + player.name + " &fjust &aconnected &fto server", LogType.COLLECTOR)
    }

    /**
     * Removes CordPlayer from cache
     *
     * @param player The bungee player
     */
    fun removePlayer(player: ProxiedPlayer) {
        val cordPlayer = getPlayer(player)

        cordexProxy.logController.log("&fPlayer " + cordPlayer.rank.chatColor + player.name + " &fjust &cleft &fthe server", LogType.COLLECTOR)

        cordexProxy.proxy.scheduler.schedule(cordexProxy, { cordexProxy.databaseController.updateQuitInfo(cordPlayer) }, 1, TimeUnit.SECONDS)

        players.remove(player.uniqueId)
    }

    /**
     * Gets CordPlayer according to Player UUID
     *
     * @param player The bungee player
     * @return The player
     */
    fun getPlayer(player: ProxiedPlayer): CordPlayer {
        return getPlayerByUniqueId(player.uniqueId)
    }

    /**
     * Gets CordPlayer according to Player UUID
     *
     * @param uuid The uuid of player
     * @return The player
     */
    fun getPlayerByUniqueId(uuid: UUID): CordPlayer {
        return players[uuid] ?: throw NullPointerException()
    }

    /**
     * Gets the all players
     *
     * @return The players
     */
    fun getPlayers(): Collection<CordPlayer> {
        return players.values
    }
}
