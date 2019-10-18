package net.minecord.cordexproxy.controller

import net.minecord.cordexproxy.CordexProxy
import net.minecord.cordexproxy.model.controller.chat.MotdStorage
import net.minecord.cordexproxy.model.controller.chat.MotdType
import net.minecord.cordexproxy.model.controller.log.LogType
import net.minecord.cordexproxy.model.controller.translation.LanguageType
import net.minecord.cordexproxy.util.colored

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.HashMap
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

class ChatController(cordexProxy: CordexProxy) : BaseController(cordexProxy) {
    private lateinit var prefixes: HashMap<String, String>
    private lateinit var motds: HashMap<LanguageType, HashMap<MotdType, ArrayList<MotdStorage>>>

    init {
        loadPrefixes()
        loadMotds()

        keepMotdsUpdated()
    }

    private fun keepMotdsUpdated() {
        cordexProxy.proxy.scheduler.schedule(cordexProxy, { motds = cordexProxy.databaseController.loadMotds() }, 15, 15, TimeUnit.SECONDS)
    }

    /**
     * Loads all prefixes from database to cache
     */
    private fun loadMotds() {
        motds = cordexProxy.databaseController.loadMotds()
        cordexProxy.logController.log("ChatController &b| &7Loaded &a" + prefixes.size + "&7 motds", LogType.INFO)
    }

    /**
     * Loads all prefixes from database to cache
     */
    private fun loadPrefixes() {
        prefixes = cordexProxy.databaseController.loadPrefixes()
        cordexProxy.logController.log("ChatController &b| &7Loaded &a" + prefixes.size + "&7 prefixes", LogType.INFO)
    }

    /**
     * Gets the prefix from cache by its name
     *
     * @param prefix The name of prefix
     * @return The requested prefix
     */
    fun getPrefix(prefix: String): String {
        return prefixes.getOrDefault(prefix, "PrefixNotFound").colored()
    }

    fun getMotds(languageType: LanguageType, motdType: MotdType): List<MotdStorage>? {
        return motds[languageType]!![motdType]
    }

    fun broadcastMessage(message: String) {
        for (cordPlayer in cordexProxy.playerController.getPlayers())
            cordPlayer.sendMessage(message)
    }

    fun broadcastMessage(prefix: String, message: String) {
        for (cordPlayer in cordexProxy.playerController.getPlayers())
            cordPlayer.sendMessage(prefix, message)
    }

    fun fixMessage(message: String): String {
        var finalMessage = message

        if (!finalMessage.startsWith("http"))
            finalMessage = finalMessage.substring(0, 1).toUpperCase() + finalMessage.substring(1)

        val lastCharacter = finalMessage.substring(finalMessage.length - 1)
        if (!(lastCharacter == "." || lastCharacter == "?" || lastCharacter == "!") && !finalMessage.endsWith(":D") && !finalMessage.endsWith(":)") && !finalMessage.endsWith(":P") && !finalMessage.endsWith(":O") && !finalMessage.endsWith(":/") && !finalMessage.endsWith(":("))
            finalMessage = "$finalMessage."

        return finalMessage
    }

    fun formatMoney(balance: Double): String {
        val symbols = DecimalFormatSymbols.getInstance()
        symbols.groupingSeparator = ','

        val formatter = DecimalFormat("###,###.##", symbols)

        return formatter.format((balance * 100.0).roundToInt() / 100.0)
    }
}
