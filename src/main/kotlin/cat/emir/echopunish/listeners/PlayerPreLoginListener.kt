package cat.emir.echopunish.listeners

import cat.emir.echolib.event.EchoListener
import cat.emir.echolib.extensions.toComponent
import cat.emir.echopunish.EchoPunish
import org.bukkit.event.EventHandler
import org.bukkit.event.player.AsyncPlayerPreLoginEvent

class PlayerPreLoginListener(val plugin: EchoPunish) : EchoListener(plugin) {

    @EventHandler
    fun onChat(event: AsyncPlayerPreLoginEvent) {
        val currentBan = plugin.punishmentDatabase.getCurrentBan(event.uniqueId)
        if (currentBan != null) {
            event.disallow(
                AsyncPlayerPreLoginEvent.Result.KICK_BANNED,
                currentBan.buildMessage() ?: "You are banned from this server.".toComponent()
            )
            return
        }

        val currentIpBan = plugin.punishmentDatabase.getCurrentIpBan(event.address.hostAddress)
        if (currentIpBan != null) {
            event.disallow(
                AsyncPlayerPreLoginEvent.Result.KICK_BANNED,
                currentIpBan.buildMessage() ?: "You are banned from this server.".toComponent()
            )
            return
        }
    }
}