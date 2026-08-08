package cat.emir.echopunish.arguments

import cat.emir.echolib.extensions.toComponent
import cat.emir.echopunish.EchoPunish
import com.mojang.brigadier.Message
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import io.papermc.paper.command.brigadier.MessageComponentSerializer
import io.papermc.paper.command.brigadier.argument.CustomArgumentType
import org.bukkit.entity.Player

import java.net.InetSocketAddress
import java.util.concurrent.CompletableFuture

class PlayerOrIPArgumentType : CustomArgumentType.Converted<PlayerOrIPArgumentType.PlayerOrIP, String> {

    data class PlayerOrIP(val player: Player?, val ip: InetSocketAddress)

    val ERROR_NO_PLAYER = SimpleCommandExceptionType(
        MessageComponentSerializer.message().serialize("<red>No player was found.</red>".toComponent())
    )

    override fun convert(nativeType: String): PlayerOrIP {
        val plugin = EchoPunish.instance
        val ipRegex = Regex("""\b(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\b""")

        if (ipRegex.matches(nativeType)) {
            return PlayerOrIP(null, InetSocketAddress.createUnresolved(nativeType, 0))
        } else {
            val player = plugin.server.getPlayer(nativeType)
            if (player != null) {
                return PlayerOrIP(player, player.address)
            }
            throw ERROR_NO_PLAYER.create()
        }
    }

    override fun getNativeType(): ArgumentType<String> {
        return StringArgumentType.string()
    }

    override fun <S : Any> listSuggestions(context: CommandContext<S>, builder: SuggestionsBuilder): CompletableFuture<Suggestions> {
        val list = mutableListOf<Pair<String, String>>()
        EchoPunish.instance.server.onlinePlayers
            .forEach {
                list.add(it.name to it.address.hostString)
                list.add(it.address.hostString to it.name)
            }

        list.filter { it.first.startsWith(builder.remaining, true) }
            .forEach {
                builder.suggest(
                    it.first,
                    MessageComponentSerializer.message().serialize(it.second.toComponent())
                )
            }

        return builder.buildFuture()
    }
}