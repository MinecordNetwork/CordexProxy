package net.minecord.cordexproxy.controller

import net.minecord.cordexproxy.CordexProxy
import net.minecord.cordexproxy.model.controller.log.LogType
import net.minecord.cordexproxy.model.controller.translation.LanguageType
import kotlin.random.Random

class TranslationController(cordexProxy: CordexProxy) : BaseController(cordexProxy) {
    private var translations: HashMap<LanguageType, HashMap<String, ArrayList<String>>>? = null

    init {
        loadTranslations()
    }

    /**
     * Loads translations from database
     */
    fun loadTranslations() {
        translations = HashMap()

        for (languageType in LanguageType.values())
            translations!![languageType] = HashMap()

        for (translation in cordexProxy.databaseController.loadTranslations()) {
            if (!translations!![translation.language]!!.containsKey(translation.string))
                translations!![translation.language]!![translation.string] = ArrayList()
            translations!![translation.language]!![translation.string]!!.add(translation.content)
        }

        var translations = 0
        for (languageType in LanguageType.values())
            for (strings in this.translations!![languageType]!!.values)
                translations += strings.size

        cordexProxy.logController.log("TranslationController &b| &7Loaded &a$translations &7translations", LogType.INFO)
    }

    /**
     * Gets translation (one form) from cache
     *
     * @param languageType The language of translation
     * @param name The name of the translation
     * @return The translated message
     */
    fun getTranslation(languageType: LanguageType, name: String): String {
        return translations!![languageType]!![name]!![0]
    }

    /**
     * Get translation (multiple forms) from cache
     */
    fun getTranslations(languageType: LanguageType, name: String): ArrayList<String>? {
        return translations!![languageType]!![name]
    }

    /**
     * Sends individual translated message to all players and console
     */
    fun broadcastTranslate(name: String) {
        for (languageType in LanguageType.values()) {
            val message = getTranslation(languageType, name)

            cordexProxy.playerController.getPlayers().stream().filter { cordPlayer -> cordPlayer.language == languageType }.forEach { cordPlayer -> cordPlayer.sendMessage(message) }

            if (languageType == LanguageType.EN)
                cordexProxy.logController.write(message)
        }
    }

    /**
     * Sends individual translated message to all players and console
     */
    fun broadcastPrefixedTranslate(prefix: String, name: String) {
        for (languageType in LanguageType.values()) {
            val message = getTranslation(languageType, name)

            cordexProxy.playerController.getPlayers().stream().filter { cordPlayer -> cordPlayer.language == languageType }.forEach { cordPlayer -> cordPlayer.sendMessage(prefix, message) }

            if (languageType == LanguageType.EN)
                cordexProxy.logController.write(message)
        }
    }

    /**
     * Sends individual translated message to all players and console with placeholders
     *
     * @param name The name of the translation
     * @param placeholders The placeholders in the name of translation
     */
    fun broadcastTranslate(name: String, placeholders: HashMap<String, String>) {
        for (languageType in LanguageType.values()) {
            var message = getTranslation(languageType, name)

            for ((key, value) in placeholders)
                message = message.replace(key, value)

            val finalMessage = message
            cordexProxy.playerController.getPlayers().stream().filter { cordPlayer -> cordPlayer.language == languageType }.forEach { cordPlayer -> cordPlayer.sendMessage(finalMessage) }

            if (languageType == LanguageType.EN)
                cordexProxy.logController.write(message)
        }
    }

    /**
     * Sends individual translated random message to all players and console
     *
     * @param name The name of the translation
     * @param placeholders The placeholders in the name of translation
     * @return True if every language has this message
     */
    fun broadcastRandomTranslate(name: String, prefix: String?, placeholders: HashMap<String, String>): Boolean {
        var size = LanguageType.values().size

        for (languageType in LanguageType.values()) {
            var messages: List<String>? = getTranslations(languageType, name)
            if (messages == null || messages.isEmpty()) {
                messages = getTranslations(LanguageType.EN, name)
                if (messages == null || messages.isEmpty())
                    continue
                size--
            }

            var message = messages[Random.nextInt(messages.size)]

            for ((key, value) in placeholders)
                message = message.replace(key, value)

            val finalMessage = message
            cordexProxy.playerController.getPlayers().stream().filter { cordPlayer -> cordPlayer.language == languageType }.forEach { cordPlayer ->
                if (prefix == null)
                    cordPlayer.sendMessage(finalMessage)
                else
                    cordPlayer.sendMessage(prefix, finalMessage)
            }

            if (languageType == LanguageType.EN)
                cordexProxy.logController.write(message)
        }

        return size != 0
    }
}