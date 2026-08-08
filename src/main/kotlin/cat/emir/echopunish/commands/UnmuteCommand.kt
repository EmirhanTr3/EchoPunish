package cat.emir.echopunish.commands

import cat.emir.echolib.command.EchoCommand
import cat.emir.echolib.sendLangMessage
import cat.emir.echopunish.EchoPunish
import cat.emir.echopunish.arguments.punishmentReasonArgument
import cat.emir.echopunish.getOfflinePlayer
import cat.emir.echopunish.nameOrUniqueId
import cat.emir.echopunish.punishment.Punishment
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import org.bukkit.entity.Player

class UnmuteCommand(plugin: EchoPunish) : EchoCommand<EchoPunish>(plugin) {
    override fun getCommand(): LiteralArgumentBuilder<CommandSourceStack> {
        return command("unmute") {
            requires { it.sender.hasPermission("echopunish.mute") }
            argument("player", ArgumentTypes.playerProfiles()) {
                punishmentReasonArgument(::execute, "You have been unmuted.")
            }
        }
    }

    fun execute(ctx: CommandContext<CommandSourceStack>, silent: Boolean, reason: String): Int {
        val sender = ctx.source.sender
        val player = ctx.getOfflinePlayer("player") ?: return 1
        if (plugin.punishmentDatabase.getCurrentMute(player.uniqueId) == null) {
            sender.sendLangMessage("mute.not", listOf("player" to player.nameOrUniqueId))
            return 1
        }

        val punishment = Punishment(
            type = Punishment.Type.UNMUTE,
            uuid = player.uniqueId,
            modUuid = if (sender is Player) sender.uniqueId else null,
            reason = reason
        )

        punishment.execute(silent)
        return 1
    }
}