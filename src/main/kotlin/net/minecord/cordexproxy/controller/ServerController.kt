package net.minecord.cordexproxy.controller

import net.minecord.cordexproxy.CordexProxy
import net.minecord.cordexproxy.model.controller.log.LogType
import net.minecord.cordexproxy.model.controller.server.ServerStorage
import java.lang.NullPointerException

import java.util.HashMap
import java.util.concurrent.TimeUnit

class ServerController(cordexProxy: CordexProxy) : BaseController(cordexProxy) {
    private val servers = HashMap<String, ServerStorage>()

    init {
        updateServers()
        cordexProxy.logController.log("ServerController &b| &7Loaded &a" + servers.size + " &7servers", LogType.INFO)
        keepServersUpdated()
    }

    /**
     * Starts updating servers every 15 seconds
     */
    private fun keepServersUpdated() {
        cordexProxy.proxy.scheduler.schedule(cordexProxy, { this.updateServers() }, 15, 15, TimeUnit.SECONDS)
    }

    /**
     * Updates servers from DB
     */
    private fun updateServers() {
        for (server in cordexProxy.databaseController.loadServers()) {
            servers[server.name] = server
        }
    }

    /**
     * Gets the server according to its name
     *
     * @param server The server name
     * @return The requested server
     */
    fun getServer(server: String): ServerStorage {
        return servers[server] ?: throw NullPointerException()
    }

    /**
     * Gets the all servers from database
     *
     * @return The all servers
     */
    fun getServers(): Collection<ServerStorage> {
        return servers.values
    }
}