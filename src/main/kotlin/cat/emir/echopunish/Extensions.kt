package cat.emir.echopunish

import cat.emir.echolib.command.EchoCommand
import cat.emir.echolib.extensions.toComponent
import cat.emir.echolib.sendLangMessage
import cat.emir.echopunish.punishment.Punishment
import com.mojang.brigadier.context.CommandContext
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.argument.resolvers.PlayerProfileListResolver
import net.kyori.adventure.text.event.HoverEvent
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.net.InetSocketAddress

fun EchoCommand<*>.ifNotExempt(
    sender: CommandSender,
    targetIp: InetSocketAddress,
    type: Punishment.Type,
    permission: String,
    callback: () -> Unit
) {
    val exemptPlayers = targetIp.findPlayers().filter { player ->
        EchoPunish.instance.luckPerms.userManager.let {
            it.getUser(player.uniqueId)?.cachedData?.permissionData?.checkPermission(permission)?.asBoolean() ?: false
        }
    }

    if (exemptPlayers.isEmpty()) {
        callback()
    } else if (exemptPlayers.size > 1) {
        sender.sendMessage("<error>You cannot ban the following players: ${exemptPlayers.joinToString(", ") { it.name }}</error>".toComponent())
    } else {
        sender.sendMessage("<error>You cannot ban ${exemptPlayers[0].name}.</error>".toComponent())
    }
}

fun EchoCommand<*>.ifNotExempt(
    sender: CommandSender,
    target: OfflinePlayer,
    type: Punishment.Type,
    permission: String,
    callback: () -> Unit
) {
    val plugin = EchoPunish.instance
    plugin.luckPerms.userManager.loadUser(target.uniqueId)
        .thenApplyAsync { user ->
            val value = user.cachedData.permissionData.checkPermission(permission).asBoolean()
            if (value) {
                sender.sendLangMessage("${type.name.lowercase()}.exempt")
            } else {
                plugin.server.scheduler.runTask(plugin, Runnable {
                    callback()
                })
            }
        }
        .exceptionally { ex ->
            plugin.logger.warning("Failed to check exemption for ${target.uniqueId}: ${ex.message}")
            sender.sendMessage(
                "<error>An error occured while checking exemption.</error>".toComponent()
                    .hoverEvent(HoverEvent.showText("<error>${ex.message}".toComponent()))
            )
        }
}

fun CommandContext<CommandSourceStack>.getOfflinePlayer(name: String): OfflinePlayer? {
    val player = this.getArgument(name, PlayerProfileListResolver::class.java).resolve(this.source)
        .firstOrNull()
        ?.id
        ?.let { EchoPunish.instance.server.getOfflinePlayer(it) }

    if (player == null) {
        this.source.sender.sendMessage("<red>No player was found</red>")
        return null
    }

    return player
}

val OfflinePlayer.nameOrUniqueId: String
    get() = this.name ?: this.uniqueId.toString()

fun InetSocketAddress.findPlayers() : List<Player> {
    return EchoPunish.instance.server.onlinePlayers
        .filter { it.address.hostString == this.hostString }
}