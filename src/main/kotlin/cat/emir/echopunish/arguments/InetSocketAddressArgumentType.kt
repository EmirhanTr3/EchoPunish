package cat.emir.echopunish.arguments

import cat.emir.echolib.extensions.toComponent
import cat.emir.echopunish.EchoPunish
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import io.papermc.paper.command.brigadier.MessageComponentSerializer
import io.papermc.paper.command.brigadier.argument.CustomArgumentType

import java.net.InetSocketAddress
import java.util.concurrent.CompletableFuture

class InetSocketAddressArgumentType : CustomArgumentType.Converted<InetSocketAddress, String> {

    val ERROR_INVALID_IP = SimpleCommandExceptionType(
        MessageComponentSerializer.message().serialize("<red>Invalid IP Address provided.</red>".toComponent())
    )

    override fun convert(nativeType: String): InetSocketAddress {
        val ipRegex = Regex("""\b(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\b""")

        if (!ipRegex.matches(nativeType)) throw ERROR_INVALID_IP.create()

        return InetSocketAddress.createUnresolved(nativeType, 0)
    }

    override fun getNativeType(): ArgumentType<String> {
        return StringArgumentType.string()
    }

    override fun <S : Any> listSuggestions(context: CommandContext<S>, builder: SuggestionsBuilder): CompletableFuture<Suggestions> {
        EchoPunish.instance.server.onlinePlayers
            .filter { it.address.hostString.startsWith(builder.remaining, true) }
            .forEach {
                builder.suggest(
                    it.address.hostString,
                    MessageComponentSerializer.message().serialize(it.name())
                )
            }

        return builder.buildFuture()
    }
}