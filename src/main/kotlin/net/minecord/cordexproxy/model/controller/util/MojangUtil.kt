package net.minecord.cordexproxy.model.controller.util

import net.minecord.cordexproxy.controller.UtilController

import java.net.MalformedURLException
import java.util.HashMap

class MojangUtil(private val utilController: UtilController) {
    private val hasPaidCache = hashMapOf<String, Boolean>()

    fun hasPaid(name: String): Boolean {
        var link: String? = null

        try {
            link = utilController.webUtil.readWebsite("http://fastapi.minecord.net/mojang/hasPaid.php?nick=$name", 4)
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        }

        if (link != null) {
            val bool = link.contains("true")
            hasPaidCache[name] = bool
            return bool
        }

        return hasPaidCache.getOrDefault(name, false)
    }
}
