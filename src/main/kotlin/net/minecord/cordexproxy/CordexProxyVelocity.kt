package net.minecord.cordexproxy

import com.google.inject.Inject
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.proxy.ProxyServer
import net.minecord.cordexproxy.autologin.AutoLoginListener
import org.slf4j.Logger


@Plugin(
    id = "cordexproxy",
    name = "CordexProxy",
    version = "1.0-SNAPSHOT",
    url = "https://minecord.net",
    description = "Core plugin for Minecord network",
    authors = ["Rixafy"]
)
class CordexProxyVelocity @Inject constructor(private val server: ProxyServer, private val logger: Logger) {
    init {
        logger.info("Hello there! I made my first plugin with Velocity.")
        //server.
    }

    @Subscribe
    fun onProxyInitialization(event: ProxyInitializeEvent?) {
        server.eventManager.register(this, AutoLoginListener(this))
    }
}
