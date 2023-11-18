package net.minecord.cordexproxy.model.controller.server

import java.sql.Timestamp

class ServerStorage(val id: Int, val name: String, val displayName: String, val gameType: String, val players: Int, val maxPlayers: Int, val ramMax: Int, val ramUsage: Int, val tps: Float, val previousStart: Timestamp, val lastStop: Timestamp) {
    val lastStart: Timestamp = Timestamp(System.currentTimeMillis())
}
