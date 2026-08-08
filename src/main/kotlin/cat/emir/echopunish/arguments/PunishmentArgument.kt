package cat.emir.echopunish.arguments

import cat.emir.echolib.extensions.toComponent
import cat.emir.echopunish.EchoPunish
import cat.emir.echopunish.punishment.ReadOnlyPunishment
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import io.papermc.paper.command.brigadier.MessageComponentSerializer
import io.papermc.paper.command.brigadier.argument.CustomArgumentType
import java.util.concurrent.CompletableFuture

class PunishmentArgument : CustomArgumentType.Converted<ReadOnlyPunishment, String> {

    val ERROR_INVALID_DURATION = SimpleCommandExceptionType(
        MessageComponentSerializer.message().serialize("<red>Punishment was not found.</red>".toComponent())
    )

    override fun convert(nativeType: String): ReadOnlyPunishment {
        return EchoPunish.instance.punishmentDatabase.getPunishment(nativeType)
            ?: throw ERROR_INVALID_DURATION.create()
    }

    override fun getNativeType(): ArgumentType<String> {
        return StringArgumentType.word()
    }

    override fun <S : Any> listSuggestions(context: CommandContext<S>, builder: SuggestionsBuilder): CompletableFuture<Suggestions> {
        EchoPunish.instance.punishmentDatabase.getAllPunishments().values
            .map { it.id }
            .filter { it.startsWith(builder.remaining, true) }
            .forEach { builder.suggest(it) }

        return builder.buildFuture()
    }
}