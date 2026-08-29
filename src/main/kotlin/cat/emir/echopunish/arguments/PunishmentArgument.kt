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
import org.bukkit.OfflinePlayer
import java.util.concurrent.CompletableFuture

class PunishmentArgument(
    val playerOnly: Boolean = false,
    val suggestionTooltip: ((ReadOnlyPunishment) -> String?)? = null,
    val filter: ((ReadOnlyPunishment) -> Boolean)? = null
) : CustomArgumentType.Converted<ReadOnlyPunishment, String> {

    val ERROR_INVALID_PUNISHMENT = SimpleCommandExceptionType(
        MessageComponentSerializer.message().serialize("<red>Punishment was not found.</red>".toComponent())
    )
    val ERROR_NO_PLAYER = SimpleCommandExceptionType(
        MessageComponentSerializer.message().serialize("<red>There are no player argument in this command.</red>".toComponent())
    )

    override fun convert(nativeType: String): ReadOnlyPunishment {
        val punishment = EchoPunish.instance.punishmentDatabase.getPunishment(nativeType)
            ?: throw ERROR_INVALID_PUNISHMENT.create()

        if (filter != null && !filter.invoke(punishment)) throw ERROR_INVALID_PUNISHMENT.create()

        return punishment
    }

    override fun getNativeType(): ArgumentType<String> {
        return StringArgumentType.word()
    }

    override fun <S : Any> listSuggestions(context: CommandContext<S>, builder: SuggestionsBuilder): CompletableFuture<Suggestions> {
        val target = if (playerOnly)
            runCatching {
                context.getArgument("player", OfflinePlayer::class.java)
            }.getOrNull() ?: throw ERROR_NO_PLAYER.create()
        else null

        EchoPunish.instance.punishmentDatabase.getAllPunishments().values
            .filter { target?.let { _ -> it.uuid == target.uniqueId } ?: true }
            .filter { filter?.invoke(it) ?: true }
            .filter { it.id.startsWith(builder.remaining, true) }
            .forEach {
                builder.suggest(it.id, suggestionTooltip?.invoke(it)
                    ?.let { tooltip -> MessageComponentSerializer.message().serialize(tooltip.toComponent()) }
                )
            }

        return builder.buildFuture()
    }
}