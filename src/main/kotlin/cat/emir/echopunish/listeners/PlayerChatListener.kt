package cat.emir.echopunish.listeners

import cat.emir.echolib.event.EchoListener
import cat.emir.echopunish.EchoPunish
import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority

class PlayerChatListener(val plugin: EchoPunish) : EchoListener(plugin) {

    @EventHandler(ignoreCancelled = true)
    fun onChat(event: AsyncChatEvent) {
        val player = event.player
        val currentMute = plugin.punishmentDatabase.getCurrentMute(player.uniqueId)
        if (currentMute != null) {
            currentMute.buildMessage()?.let { player.sendMessage(it) }
            event.isCancelled = true
            return
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onChatContext(event: AsyncChatEvent) {
        val player = event.player
        val message = MiniMessage.miniMessage().serialize(event.renderer().render(player, player.displayName(), event.message(), Audience.empty()))

        plugin.chatContextManager.addMessage(player, message)
    }
}