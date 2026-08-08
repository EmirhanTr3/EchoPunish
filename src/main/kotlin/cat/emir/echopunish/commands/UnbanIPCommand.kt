package cat.emir.echopunish.commands

import cat.emir.echolib.command.EchoCommand
import cat.emir.echolib.sendLangMessage
import cat.emir.echopunish.EchoPunish
import cat.emir.echopunish.arguments.InetSocketAddressArgumentType
import cat.emir.echopunish.punishment.Punishment
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.bukkit.entity.Player
import java.net.InetSocketAddress

class UnbanIPCommand(plugin: EchoPunish) : EchoCommand<EchoPunish>(plugin) {

    override val aliases = setOf("unipban")

    override fun getCommand(): LiteralArgumentBuilder<CommandSourceStack> {
        return command("unbanip") {
            requires { it.sender.hasPermission("echopunish.banip") || it.sender.hasPermission("echopunish.tempbanip") }
            argument("ip", InetSocketAddressArgumentType()) {
                argument("reason", StringArgumentType.greedyString()) {
                    executes { ctx -> executeIp(ctx, StringArgumentType.getString(ctx, "reason")) }
                }
                executes { ctx -> executeIp(ctx, "You have been unbanned.") }
            }
        }
    }

    fun executeIp(ctx: CommandContext<CommandSourceStack>, reason: String): Int {
        val sender = ctx.source.sender
        val ip = ctx.getArgument("ip", InetSocketAddress::class.java)
        if (plugin.punishmentDatabase.getCurrentIpBan(ip.hostString) == null) {
            sender.sendLangMessage("banip.not", listOf("player" to ip.hostString))
            return 1
        }

        val punishment = Punishment(
            type = Punishment.Type.UNBANIP,
            ip = ip,
            modUuid = if (sender is Player) sender.uniqueId else null,
            reason = reason
        )

        punishment.execute(true)
        return 1
    }
}