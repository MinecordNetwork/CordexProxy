package net.minecord.cordexproxy.autologin

import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.LoginEvent
import com.velocitypowered.api.event.connection.PostLoginEvent
import com.velocitypowered.api.event.connection.PreLoginEvent
import net.minecord.cordexproxy.CordexProxyVelocity

class AutoLoginListener(var cordexProxyVelocity: CordexProxyVelocity) {
    @Subscribe
    fun onPreLogin(e: PreLoginEvent) {
        //e.connection.
        //e.result = PreLoginEvent.PreLoginComponentResult.forceOnlineMode()
    }

    @Subscribe
    fun onPreLogin(e: LoginEvent) {
        //e.
    }

    @Subscribe
    fun onPreLogin(e: PostLoginEvent) {
        //e.player.conn
    }
}