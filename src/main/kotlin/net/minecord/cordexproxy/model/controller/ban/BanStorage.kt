package net.minecord.cordexproxy.model.controller.ban

import java.sql.Timestamp

class BanStorage(var id: Int, var targetId: Int, var targetIp: Int, var adminId: Int, var adminIp: Int, var reason: String, var expire: Timestamp, var isIpBan: Boolean, var isActive: Boolean)
