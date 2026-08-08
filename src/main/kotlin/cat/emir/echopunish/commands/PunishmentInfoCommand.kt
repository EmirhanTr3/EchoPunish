package cat.emir.echopunish.commands

import cat.emir.echolib.command.EchoCommand
import cat.emir.echolib.command.getPlayer
import cat.emir.echopunish.EchoPunish
import cat.emir.echopunish.arguments.PunishmentArgument
import cat.emir.echopunish.punishment.ReadOnlyPunishment
import cat.emir.echopunish.uis.PunishmentInfoDialog
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import io.papermc.paper.command.brigadier.CommandSourceStack

class PunishmentInfoCommand(plugin: EchoPunish) : EchoCommand<EchoPunish>(plugin) {
    override fun getCommand(): LiteralArgumentBuilder<CommandSourceStack> {
        return command("pinfo") {
            requires { it.sender.hasPermission("echopunish.pinfo") }
            argument("punishment", PunishmentArgument()) {
                executes(::execute)
            }
        }
    }

    fun execute(ctx: CommandContext<CommandSourceStack>): Int {
        val player = ctx.getPlayer() ?: return 1
        val punishment = ctx.getArgument("punishment", ReadOnlyPunishment::class.java)

        PunishmentInfoDialog(plugin, player, punishment).showDialog()

        return 1
    }
}