package net.minecord.cordexproxy.model.controller.player

import net.md_5.bungee.api.ChatColor
import net.minecord.cordexproxy.util.colored
import java.time.LocalDate

class RankStorage(val name: String, val basicPermission: String, var stringColor: String, val chatColor: ChatColor, val integerColor: Int) {
    private val aprilFool = LocalDate.now().month.value == 4 && LocalDate.now().dayOfMonth == 1

    init {
        if (aprilFool) {
            stringColor = when (basicPermission) {
                "owner.global",
                "admin.global",
                "builder.global",
                "support.global",
                "mapmaker.global",
                "trainee.global",
                "premium.global" -> "&#447eff"
                else -> "&c"
            }
        }
    }

    val isAdmin: Boolean
        get() {
            return when (basicPermission) {
                "owner.global" -> true
                "admin.global" -> true
                "builder.global" -> true
                "support.global" -> true
                "mapmaker.global" -> true
                "trainee.global" -> true
                else -> false
            }
        }

    val prefix: String
        get() {
            if (aprilFool) {
                return when (basicPermission) {
                    "owner.global",
                    "admin.global",
                    "builder.global",
                    "support.global",
                    "mapmaker.global",
                    "trainee.global",
                    "premium.global" -> ("&#447eff&l" + "PLEB").colored() + ChatColor.RESET + " "
                    else -> ("&c&l" + "ADMIN").colored() + ChatColor.RESET + " "
                }
            }

            return (stringColor + "&l" + name.toUpperCase()).colored() + ChatColor.RESET + " "
        }
}
