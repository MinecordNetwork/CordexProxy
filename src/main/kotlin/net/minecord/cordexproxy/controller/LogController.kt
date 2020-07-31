package net.minecord.cordexproxy.controller

import net.minecord.cordexproxy.CordexProxy
import net.minecord.cordexproxy.model.controller.log.LogType
import net.minecord.cordexproxy.util.colored

class LogController(cordexProxy: CordexProxy) : BaseController(cordexProxy) {
    fun log(message: String, logType: LogType) {
        cordexProxy.logger.info((logType.chatColor.toString() + "[" + logType.toString() + "] &7" + message).colored())
    }

    fun write(message: String) {
        cordexProxy.logger.info(message.colored())
    }
}
