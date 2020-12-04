package net.minecord.cordexproxy.model.controller.ban

import java.sql.Timestamp
import java.util.UUID

class MuteStorage(var playerId: Int, var uuid: UUID?, var playerName: String, var reason: String?, var mutedAt: Timestamp?, var expireAt: Timestamp?)
