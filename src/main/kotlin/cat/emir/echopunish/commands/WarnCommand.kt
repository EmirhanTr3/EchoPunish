package cat.emir.echopunish.commands

import cat.emir.echolib.command.EchoCommand
import cat.emir.echolib.command.arguments.CachedOfflinePlayerArgument
import cat.emir.echolib.command.arguments.getOfflinePlayer
import cat.emir.echopunish.EchoPunish
import cat.emir.echopunish.arguments.punishmentReasonArgument
import cat.emir.echopunish.ifNotExempt
import cat.emir.echopunish.punishment.Punishment
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.bukkit.entity.Player

class WarnCommand(plugin: EchoPunish) : EchoCommand<EchoPunish>(plugin) {
    override fun getCommand(): LiteralArgumentBuilder<CommandSourceStack> {
        return command("warn") {
            requires { it.sender.hasPermission("echopunish.warn") }
            argument("player", CachedOfflinePlayerArgument(true)) {
                punishmentReasonArgument(::execute, "You have been warned.")
            }
        }
    }

    fun execute(ctx: CommandContext<CommandSourceStack>, silent: Boolean, reason: String): Int {
        val sender = ctx.source.sender
        val player = ctx.getOfflinePlayer("player") ?: return 1

        ifNotExempt(sender, player, Punishment.Type.WARN, "echopunish.warn.exempt") {
            val punishment = Punishment(
                type = Punishment.Type.WARN,
                uuid = player.uniqueId,
                modUuid = if (sender is Player) sender.uniqueId else null,
                reason = reason,
                chatContext = plugin.chatContextManager.createContext(player),
            )

            punishment.execute(silent)
        }
        return 1
    }
}