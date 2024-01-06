package net.minecord.cordexproxy.controller

import net.minecord.cordexproxy.CordexProxy
import net.minecord.cordexproxy.model.controller.log.LogType
import net.minecord.cordexproxy.model.controller.player.RankStorage
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.connection.ProxiedPlayer
import java.util.HashMap

class RankController(cordexProxy: CordexProxy) : BaseController(cordexProxy) {
    private val ranks = HashMap<Int, RankStorage>()

    init {
        ranks[0] = RankStorage("Owner", "owner.global", "&c", ChatColor.RED, 0xFB5454)
        ranks[1] = RankStorage("Developer", "developer.global", "&c", ChatColor.RED, 0xFB5454)
        ranks[2] = RankStorage("Admin", "admin.global", "&c", ChatColor.RED, 0xFB5454)
        ranks[3] = RankStorage("Builder", "builder.global", "&c", ChatColor.RED, 0xFB5454)
        ranks[4] = RankStorage("Support", "support.global", "&c", ChatColor.RED, 0xFB5454)
        ranks[5] = RankStorage("Trainee", "trainee.global", "&c", ChatColor.RED, 0xFB5454)
        ranks[6] = RankStorage("Streamer", "streamer.global", "&a", ChatColor.AQUA, 0xFB5454)
        ranks[7] = RankStorage("VIP", "premium.global", "&a", ChatColor.GREEN, 0x54FB54)
        ranks[8] = RankStorage("Member", "default.global", "&#447eff", ChatColor.BLUE, 0x437DFB)


        cordexProxy.logController.log("RankController &b| &7Loaded &a${ranks.size} &7ranks", LogType.INFO)
    }

    fun getRank(player: ProxiedPlayer): RankStorage {
        for (value in ranks.values) {
            if (player.hasPermission(value.basicPermission))
                return value
        }

        return ranks[ranks.size - 1]!!
    }

    fun getRank(name: String): RankStorage {
        var target = name

        if (target == "premium") {
            target = "vip"
        }

        if (target.equals("default", ignoreCase = true)) {
            target = "Member"
        }

        for (value in ranks.values) {
            if (value.name.equals(target, ignoreCase = true))
                return value
        }

        return ranks[ranks.size - 1]!!
    }

    /**
     * Gets all ranks
     *
     * @return The collection of all ranks
     */
    fun getRanks(): Collection<RankStorage> {
        return ranks.values
    }
}
