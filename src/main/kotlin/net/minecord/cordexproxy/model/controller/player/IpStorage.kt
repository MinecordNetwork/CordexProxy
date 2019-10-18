package net.minecord.cordexproxy.model.controller.player

import net.minecord.cordexproxy.model.controller.translation.LanguageType

class IpStorage(var id: Int, var country: String, var currency: String, language: String) {
    lateinit var language: LanguageType
        private set

    init {
        setLanguage(language)
    }

    fun setLanguage(language: String) {
        var finalLanguage = language
        if (finalLanguage != "cs" && finalLanguage != "sk")
            finalLanguage = "en"
        else if (finalLanguage == "sk")
            finalLanguage = "cs"
        this.language = LanguageType.valueOf(finalLanguage.toUpperCase())
    }
}
