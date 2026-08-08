package cat.emir.echopunish.commands

import cat.emir.echolib.command.EchoCommand
import cat.emir.echopunish.EchoPunish
import cat.emir.echopunish.arguments.PlayerOrIPArgumentType
import cat.emir.echopunish.ifNotExempt
import cat.emir.echopunish.punishment.Punishment
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.bukkit.entity.Player
import java.net.InetSocketAddress

class BanIPCommand(plugin: EchoPunish) : EchoCommand<EchoPunish>(plugin) {

    override val aliases = setOf("ipban")

    override fun getCommand(): LiteralArgumentBuilder<CommandSourceStack> {
        return command("banip") {
            requires { it.sender.hasPermission("echopunish.banip") }
            argument("ip", PlayerOrIPArgumentType()) {
                argument("reason", StringArgumentType.greedyString()) {
                    executes { ctx -> execute(ctx, StringArgumentType.getString(ctx, "reason")) }
                }
                executes { ctx -> execute(ctx, "You have been banned.") }
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

        ifNotExempt(sender, player, Punishment.Type.BANIP, "echopunish.banip.exempt") {
            val punishment = Punishment(
                type = Punishment.Type.BANIP,
                ip = player.address,
                modUuid = if (sender is Player) sender.uniqueId else null,
                reason = reason,
                chatContext = plugin.chatContextManager.createContext(player),
            )

            punishment.execute(true)
        }
        return 1
    }

    fun executeIp(ctx: CommandContext<CommandSourceStack>, ip: InetSocketAddress, reason: String): Int {
        val sender = ctx.source.sender

        ifNotExempt(sender, ip, Punishment.Type.BANIP, "echopunish.banip.exempt") {
            val punishment = Punishment(
                type = Punishment.Type.BANIP,
                ip = ip,
                modUuid = if (sender is Player) sender.uniqueId else null,
                reason = reason
            )

            punishment.execute(true)
        }
        return 1
    }
}