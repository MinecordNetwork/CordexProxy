package net.minecord.cordexproxy.model.controller.player

import net.minecord.cordexproxy.CordexProxy
import net.minecord.cordexproxy.model.controller.translation.LanguageType
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.minecord.cordexproxy.util.colored

import java.util.HashMap

class CordPlayer(private val cordexProxy: CordexProxy, val player: ProxiedPlayer, val data: PlayerStorage) {
    val rank: RankStorage
    val language: LanguageType
    var hidden = false

    init {
        val ipData = cordexProxy.cacheController.getIpData(data.lastIpAddress)
        this.rank = cordexProxy.rankController.getRank(player)
        this.language = ipData.language
    }

    /**
     * Translates the message according to language of player
     *
     * @param name The name of the translation
     * @return The translated message
     */
    fun translateMessage(name: String): String {
        return cordexProxy.translationController.getTranslation(language, name)
    }

    /**
     * Sends the message to player
     *
     * @param message The message
     */
    fun sendMessage(message: String) {
        TextComponent()
        player.sendMessage(ChatMessageType.CHAT, *TextComponent.fromLegacyText(message.colored()))
    }

    /**
     * Sends the message to player with some prefix
     *
     * @param message The message
     * @param prefix The name of the prefix
     */
    fun sendMessage(prefix: String, message: String) {
        player.sendMessage(ChatMessageType.CHAT, *TextComponent.fromLegacyText(message.colored().replace("%prefix%", cordexProxy.chatController.getPrefix(prefix))))
    }

    /**
     * Sends the message to player with some prefix
     *
     * @param message The message
     * @param prefix The name of the prefix
     * @param placeholders The translation placeholders
     */
    fun sendMessage(prefix: String, message: String, placeholders: HashMap<String, String>) {
        val msg = arrayOf(message)
        placeholders.forEach { (key, value) -> msg[0] = msg[0].replace(key, value) }
        player.sendMessage(ChatMessageType.CHAT, *TextComponent.fromLegacyText(msg[0].colored().replace("%prefix%", cordexProxy.chatController.getPrefix(prefix))))
    }

    /**
     * Sends the message to player with some prefix
     *
     * @param message The message
     */
    fun sendLiveMessage(message: BaseComponent) {
        player.sendMessage(ChatMessageType.CHAT, message)
    }

    /**
     * Sends the action bar message to player
     *
     * @param message The message
     */
    fun sendActionBar(message: String) {
        player.sendMessage(ChatMessageType.ACTION_BAR, *TextComponent.fromLegacyText(message.colored()))
    }

    fun onVanish() {
        hidden = !hidden
    }

    fun show() {
        hidden = false
    }
}
