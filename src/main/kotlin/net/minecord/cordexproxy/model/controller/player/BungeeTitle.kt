package net.minecord.cordexproxy.model.controller.player

import net.md_5.bungee.api.Title
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.chat.ComponentSerializer
import net.md_5.bungee.protocol.DefinedPacket
import net.md_5.bungee.protocol.packet.Title.Action

class BungeeTitle : Title {
    private var title: net.md_5.bungee.protocol.packet.Title? = null
    private var subtitle: net.md_5.bungee.protocol.packet.Title? = null
    private var times: net.md_5.bungee.protocol.packet.Title? = null
    private var clear: net.md_5.bungee.protocol.packet.Title? = null
    private var reset: net.md_5.bungee.protocol.packet.Title? = null

    private fun createPacket(action: Action): net.md_5.bungee.protocol.packet.Title {
        val title = net.md_5.bungee.protocol.packet.Title()
        title.action = action

        if (action == Action.TIMES) {
            title.fadeIn = 20
            title.stay = 60
            title.fadeOut = 20
        }
        return title
    }

    override fun title(text: BaseComponent): Title {
        if (title == null) {
            title = createPacket(Action.TITLE)
        }

        title!!.text = ComponentSerializer.toString(text)
        return this
    }

    override fun title(vararg text: BaseComponent): Title {
        if (title == null) {
            title = createPacket(Action.TITLE)
        }

        title!!.text = ComponentSerializer.toString(*text)
        return this
    }

    override fun subTitle(text: BaseComponent): Title {
        if (subtitle == null) {
            subtitle = createPacket(Action.SUBTITLE)
        }

        subtitle!!.text = ComponentSerializer.toString(text)
        return this
    }

    override fun subTitle(vararg text: BaseComponent): Title {
        if (subtitle == null) {
            subtitle = createPacket(Action.SUBTITLE)
        }

        subtitle!!.text = ComponentSerializer.toString(*text)
        return this
    }

    override fun fadeIn(ticks: Int): Title {
        if (times == null) {
            times = createPacket(Action.TIMES)
        }

        times!!.fadeIn = ticks
        return this
    }

    override fun stay(ticks: Int): Title {
        if (times == null) {
            times = createPacket(Action.TIMES)
        }

        times!!.stay = ticks
        return this
    }

    override fun fadeOut(ticks: Int): Title {
        if (times == null) {
            times = createPacket(Action.TIMES)
        }

        times!!.fadeOut = ticks
        return this
    }

    override fun clear(): Title {
        if (clear == null) {
            clear = createPacket(Action.CLEAR)
        }

        title = null

        return this
    }

    override fun reset(): Title {
        if (reset == null) {
            reset = createPacket(Action.RESET)
        }

        title = null
        subtitle = null
        times = null

        return this
    }

    private fun sendPacket(player: ProxiedPlayer, packet: DefinedPacket?) {
        if (packet != null) {
            player.unsafe().sendPacket(packet)
        }
    }

    override fun send(player: ProxiedPlayer): Title {
        sendPacket(player, clear)
        sendPacket(player, reset)
        sendPacket(player, times)
        sendPacket(player, subtitle)
        sendPacket(player, title)
        return this
    }
}
