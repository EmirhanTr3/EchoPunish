package cat.emir.echopunish.arguments

import cat.emir.echolib.extensions.toComponent
import cat.emir.echolib.extensions.toDuration
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import io.papermc.paper.command.brigadier.MessageComponentSerializer
import io.papermc.paper.command.brigadier.argument.CustomArgumentType
import java.time.Duration
import java.util.concurrent.CompletableFuture

class DurationArgument : CustomArgumentType.Converted<Duration, String> {

    val ERROR_INVALID_DURATION = SimpleCommandExceptionType(
        MessageComponentSerializer.message().serialize("<red>Invalid duration provided.</red>".toComponent())
    )

    override fun convert(nativeType: String): Duration {
        return nativeType.toDuration() ?: throw ERROR_INVALID_DURATION.create()
    }

    override fun getNativeType(): ArgumentType<String> {
        return StringArgumentType.string()
    }

    override fun <S : Any> listSuggestions(context: CommandContext<S>, builder: SuggestionsBuilder): CompletableFuture<Suggestions> {
        listOf("1h", "12h", "1d", "3d", "1w", "2w", "1mo", "6mo", "1y")
            .filter { it.startsWith(builder.remaining, true) }
            .forEach { builder.suggest(it) }

        return builder.buildFuture()
    }
}