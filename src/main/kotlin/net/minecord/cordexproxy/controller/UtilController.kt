package net.minecord.cordexproxy.controller

import net.minecord.cordexproxy.CordexProxy
import net.minecord.cordexproxy.model.controller.util.MojangUtil
import net.minecord.cordexproxy.model.controller.util.WebUtil

class UtilController(cordexProxy: CordexProxy) : BaseController(cordexProxy) {
    val mojangUtil: MojangUtil = MojangUtil(this)
    val webUtil: WebUtil = WebUtil(this)
}
