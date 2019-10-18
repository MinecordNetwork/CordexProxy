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
        ranks[0] = RankStorage("Owner", "owner.global", "&c&l", ChatColor.RED)
        ranks[1] = RankStorage("Admin", "admin.global", "&c&l", ChatColor.RED)
        ranks[2] = RankStorage("Builder", "builder.global", "&c&l", ChatColor.RED)
        ranks[3] = RankStorage("Support", "support.global", "&c&l", ChatColor.RED)
        ranks[5] = RankStorage("Trainee", "trainee.global", "&6&l", ChatColor.GOLD)
        ranks[6] = RankStorage("YouTuber", "youtuber.global", "&c&l", ChatColor.RED)
        ranks[7] = RankStorage("Hero", "hero.global", "&b&l", ChatColor.AQUA)
        ranks[8] = RankStorage("Magic", "magic.global", "&a&l", ChatColor.GREEN)
        ranks[9] = RankStorage("Basic", "basic.global", "&e&l", ChatColor.YELLOW)
        ranks[10] = RankStorage("Member", "default.global", "&9&l", ChatColor.BLUE)

        cordexProxy.logController.log("RankController &b| &7Loaded &a${ranks.size} &7ranks", LogType.INFO)
    }

    /**
     * Gets the current rank of player according to basic permission
     *
     * @param player The bukkit player
     * @return The rank
     */
    fun getRank(player: ProxiedPlayer): RankStorage {
        for (value in ranks.values) {
            if (player.hasPermission(value.basicPermission))
                return value
        }
        return RankStorage("Member", "default.global", "&9&l", ChatColor.BLUE)
    }

    /**
     * Gets the current rank by name
     *
     * @param name The rank name
     * @return The added player
     */
    fun getRank(name: String): RankStorage {
        for (value in ranks.values) {
            if (value.name.toLowerCase() == name.toLowerCase())
                return value
        }

        return ranks[10] ?: throw NullPointerException()
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
