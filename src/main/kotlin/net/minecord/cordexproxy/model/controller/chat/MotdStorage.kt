package net.minecord.cordexproxy.model.controller.chat

import net.minecord.cordexproxy.model.controller.translation.LanguageType

class MotdStorage(val type: MotdType, val firstPayload: String, val isFirstCentered: Boolean, val secondPayload: String, val isSecondCentered: Boolean, val language: LanguageType)
