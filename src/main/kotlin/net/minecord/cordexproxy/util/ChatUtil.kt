package net.minecord.cordexproxy.util

import net.md_5.bungee.api.ChatColor
import net.minecord.cordexproxy.model.controller.chat.DefaultFontInfo
import java.util.regex.Matcher
import java.util.regex.Pattern

private val REPLACE_ALL_RGB_PATTERN: Pattern = Pattern.compile("(&)?&#([0-9a-fA-F]{6})")

fun String.colored(colorChar: Char = '&'): String {
    val rgbBuilder = StringBuffer()
    val rgbMatcher: Matcher = REPLACE_ALL_RGB_PATTERN.matcher(this)

    while (rgbMatcher.find()) {
        val isEscaped = rgbMatcher.group(1) != null
        if (!isEscaped) {
            try {
                val hexCode: String = rgbMatcher.group(2)
                rgbMatcher.appendReplacement(rgbBuilder, ChatColor.of(parseHexColor(hexCode)).toString())
                continue
            } catch (ignored: NumberFormatException) {
            }
        }
        rgbMatcher.appendReplacement(rgbBuilder, "&#$2")
    }

    rgbMatcher.appendTail(rgbBuilder)

    return ChatColor.translateAlternateColorCodes(colorChar, rgbBuilder.toString())
}

private fun parseHexColor(hexColor: String): String? {
    var color = hexColor

    if (!color.startsWith("#")) {
        color = "#$color"
    }

    if (color.length != 7) {
        return "#FFFFFF"
    }

    return color
}

fun String.centerMessage(center_pixel: Int): String {
    var messagePxSize = 0
    var previousCode = false
    var isBold = false

    for (c in this.toCharArray()) {
        when {
            c == '§' -> previousCode = true
            previousCode -> {
                previousCode = false
                isBold = c == 'l' || c == 'L'
            }
            else -> {
                val dFI = DefaultFontInfo.getDefaultFontInfo(c)
                messagePxSize += if (isBold) dFI.boldLength else dFI.length
                messagePxSize++
            }
        }
    }

    val halvedMessageSize = messagePxSize / 2
    val toCompensate = center_pixel - halvedMessageSize
    val spaceLength = DefaultFontInfo.SPACE.length + 1
    var compensated = 0

    val sb = StringBuilder()
    while (compensated < toCompensate) {
        sb.append(" ")
        compensated += spaceLength
    }

    return sb.toString() + this
}

fun String.centerChatMessage(): String {
    return this.centerMessage(154)
}

fun String.centerMotdMessage(): String {
    return this.centerMessage(128)
}

fun Int.formatTime(): String {
    val sec = this / 1000

    val hours = sec / 3600
    val minutes = sec % 3600 / 60
    val seconds = sec % 60

    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}
