package cat.emir.echopunish.commands

import cat.emir.echolib.command.EchoCommand
import cat.emir.echopunish.EchoPunish
import cat.emir.echopunish.arguments.DurationArgument
import cat.emir.echopunish.arguments.punishmentReasonArgument
import cat.emir.echopunish.getOfflinePlayer
import cat.emir.echopunish.ifNotExempt
import cat.emir.echopunish.punishment.Punishment
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import org.bukkit.entity.Player
import java.time.Duration

class MuteCommand(plugin: EchoPunish) : EchoCommand<EchoPunish>(plugin) {
    override fun getCommand(): LiteralArgumentBuilder<CommandSourceStack> {
        return command("mute") {
            requires { it.sender.hasPermission("echopunish.mute") }
            argument("player", ArgumentTypes.playerProfiles()) {
                argument("duration", DurationArgument()) {
                    punishmentReasonArgument(::execute, "You have been muted.")
                }
            }
        }
    }

    fun execute(ctx: CommandContext<CommandSourceStack>, silent: Boolean, reason: String): Int {
        val sender = ctx.source.sender
        val player = ctx.getOfflinePlayer("player") ?: return 1
        val duration = ctx.getArgument("duration", Duration::class.java)

        ifNotExempt(sender, player, Punishment.Type.MUTE, "echopunish.mute.exempt") {
            val punishment = Punishment(
                type = Punishment.Type.MUTE,
                uuid = player.uniqueId,
                modUuid = if (sender is Player) sender.uniqueId else null,
                duration = duration,
                reason = reason,
                chatContext = plugin.chatContextManager.createContext(player),
            )

            punishment.execute(silent)
        }
        return 1
    }
}