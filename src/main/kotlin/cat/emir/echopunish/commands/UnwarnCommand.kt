package cat.emir.echopunish.commands

import cat.emir.echolib.command.EchoCommand
import cat.emir.echolib.command.arguments.CachedOfflinePlayerArgument
import cat.emir.echolib.command.arguments.getOfflinePlayer
import cat.emir.echopunish.EchoPunish
import cat.emir.echopunish.arguments.PunishmentArgument
import cat.emir.echopunish.arguments.punishmentReasonArgument
import cat.emir.echopunish.punishment.Punishment
import cat.emir.echopunish.punishment.ReadOnlyPunishment
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.bukkit.entity.Player

class UnwarnCommand(plugin: EchoPunish) : EchoCommand<EchoPunish>(plugin) {
    override fun getCommand(): LiteralArgumentBuilder<CommandSourceStack> {
        return command("unwarn") {
            requires { it.sender.hasPermission("echopunish.warn") }
            argument("player", CachedOfflinePlayerArgument(true)) {
                argument("warn", PunishmentArgument(true, { it.reason }) {
                    it.type == Punishment.Type.WARN && !it.isUnwarned
                }) {
                    punishmentReasonArgument(::execute, "You have been unwarned.")
                }
            }
        }
    }

    fun execute(ctx: CommandContext<CommandSourceStack>, silent: Boolean, reason: String): Int {
        val sender = ctx.source.sender
        val player = ctx.getOfflinePlayer("player")
        val warn = ctx.getArgument("warn", ReadOnlyPunishment::class.java)

        val punishment = Punishment(
            type = Punishment.Type.UNWARN,
            uuid = player.uniqueId,
            modUuid = if (sender is Player) sender.uniqueId else null,
            reason = reason,
            targetPunishmentId = warn.id
        )

        punishment.execute(silent)
        return 1
    }
}