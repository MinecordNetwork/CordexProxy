package net.minecord.cordexproxy.model.controller.util

import net.minecord.cordexproxy.controller.UtilController

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL

class WebUtil(private val utilController: UtilController) {
    @Throws(MalformedURLException::class)
    fun readWebsite(link: String, timeout: Int): String? {
        val url = URL(link)

        return try {
            HttpURLConnection.setFollowRedirects(false)
            val connection = url.openConnection()

            connection.connectTimeout = timeout * 1000
            connection.readTimeout = timeout * 1000

            BufferedReader(InputStreamReader(connection.getInputStream())).use(BufferedReader::readText)

        } catch (e: IOException) {
            null
        }
    }
}
