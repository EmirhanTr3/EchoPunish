package cat.emir.echopunish.commands

import cat.emir.echolib.command.EchoCommand
import cat.emir.echopunish.EchoPunish
import cat.emir.echopunish.arguments.DurationArgument
import cat.emir.echopunish.arguments.PlayerOrIPArgumentType
import cat.emir.echopunish.ifNotExempt
import cat.emir.echopunish.punishment.Punishment
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.bukkit.entity.Player
import java.net.InetSocketAddress
import java.time.Duration

class TempBanIPCommand(plugin: EchoPunish) : EchoCommand<EchoPunish>(plugin) {

    override val aliases = setOf("iptempban")

    override fun getCommand(): LiteralArgumentBuilder<CommandSourceStack> {
        return command("tempbanip") {
            requires { it.sender.hasPermission("echopunish.tempbanip") }
            argument("ip", PlayerOrIPArgumentType()) {
                argument("duration", DurationArgument()) {
                    argument("reason", StringArgumentType.greedyString()) {
                        executes { ctx -> execute(ctx, StringArgumentType.getString(ctx, "reason")) }
                    }
                    executes { ctx -> execute(ctx, "You have been banned.") }
                }
            }
        }
    }

    fun execute(ctx: CommandContext<CommandSourceStack>, reason: String): Int {
        val playerOrIp = ctx.getArgument("ip", PlayerOrIPArgumentType.PlayerOrIP::class.java)
        if (playerOrIp.player != null) {
            return executePlayer(ctx, playerOrIp.player, reason)
        }
        return executeIp(ctx, playerOrIp.ip, reason)
    }

    fun executePlayer(ctx: CommandContext<CommandSourceStack>, player: Player, reason: String): Int {
        val sender = ctx.source.sender
        val duration = ctx.getArgument("duration", Duration::class.java)

        ifNotExempt(sender, player, Punishment.Type.TEMPBANIP, "echopunish.tempbanip.exempt") {
            val punishment = Punishment(
                type = Punishment.Type.TEMPBANIP,
                ip = player.address,
                modUuid = if (sender is Player) sender.uniqueId else null,
                duration = duration,
                reason = reason,
                chatContext = plugin.chatContextManager.createContext(player),
            )

            punishment.execute(true)
        }
        return 1
    }

    fun executeIp(ctx: CommandContext<CommandSourceStack>, ip: InetSocketAddress, reason: String): Int {
        val sender = ctx.source.sender
        val duration = ctx.getArgument("duration", Duration::class.java)

        ifNotExempt(sender, ip, Punishment.Type.TEMPBANIP, "echopunish.tempbanip.exempt") {
            val punishment = Punishment(
                type = Punishment.Type.TEMPBANIP,
                ip = ip,
                modUuid = if (sender is Player) sender.uniqueId else null,
                duration = duration,
                reason = reason
            )

            punishment.execute(true)
        }
        return 1
    }
}