package cat.emir.echopunish.commands

import cat.emir.echopunish.uis.HistoryGUI
import cat.emir.echolib.command.EchoCommand
import cat.emir.echolib.command.arguments.CachedOfflinePlayerArgument
import cat.emir.echolib.command.arguments.getOfflinePlayer
import cat.emir.echolib.command.getPlayer
import cat.emir.echolib.extensions.toComponent
import cat.emir.echopunish.EchoPunish
import cat.emir.echopunish.nameOrUniqueId
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import io.papermc.paper.command.brigadier.CommandSourceStack

class HistoryCommand(plugin: EchoPunish) : EchoCommand<EchoPunish>(plugin) {
    override fun getCommand(): LiteralArgumentBuilder<CommandSourceStack> {
        return command("history") {
            requires { it.sender.hasPermission("echopunish.history") }
            argument("player", CachedOfflinePlayerArgument(true)) {
                executes(::execute)
            }
        }
    }

    fun execute(ctx: CommandContext<CommandSourceStack>): Int {
        val player = ctx.getPlayer() ?: return 1
        val target = ctx.getOfflinePlayer("player") ?: return 1

        if (plugin.punishmentDatabase.getAllPunishments(target.uniqueId).isEmpty()) {
            player.sendMessage("<error>${target.nameOrUniqueId} has no punishments.</error>".toComponent())
            return 1
        }

        HistoryGUI(plugin, player, target).openGUI()

        return 1
    }
}