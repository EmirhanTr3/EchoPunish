package cat.emir.echopunish.punishment

import cat.emir.echolib.extensions.toComponent
import cat.emir.echolib.extensions.toReadableString
import cat.emir.echopunish.EchoPunish
import cat.emir.echopunish.findPlayers
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import java.net.InetSocketAddress
import java.time.Duration
import java.time.Instant
import java.util.*

open class Punishment(
    val id: String = generateId(),
    val type: Type,
    val uuid: UUID? = null,
    val ip: InetSocketAddress? = null,
    val modUuid: UUID? = null,
    val duration: Duration? = null,
    val issuedAt: Instant = Instant.now(),
    val reason: String,
    val chatContext: List<String>? = null
) {
    val plugin = EchoPunish.instance
    val player = uuid?.let { plugin.server.getOfflinePlayer(it) }
    val mod = modUuid?.let { plugin.server.getOfflinePlayer(it) }
    val timeLeft: Duration?
        get() {
            if (duration == null) return null
            val expiresAt = issuedAt.plus(duration)
            val remaining = Duration.between(Instant.now(), expiresAt)
            return if (remaining.isNegative) Duration.ZERO else remaining
        }

    enum class Type {
        WARN,
        KICK,
        MUTE,
        BAN,
        TEMPBAN,
        BANIP,
        TEMPBANIP,
        UNMUTE,
        UNBAN,
        UNBANIP
    }

    companion object {
        fun generateId() = "P" + System.currentTimeMillis()
    }

    private val resolvers = listOf(
        Placeholder.unparsed("id", id),
        Placeholder.unparsed("player", player?.name ?: player?.uniqueId?.toString() ?: ip?.hostString ?: "null"),
        Placeholder.unparsed("mod", mod?.name ?: modUuid?.toString() ?: "Server"),
        Placeholder.unparsed("duration", duration?.toReadableString() ?: "null"),
        Placeholder.unparsed("reason", reason),
        Placeholder.unparsed("timeleft", timeLeft?.toReadableString() ?: "null"),
    ).toTypedArray()

    fun buildMessage() = plugin.lang.getOrNull("${type.toString().lowercase()}.message")?.toComponent(*resolvers)

    open fun execute(silent: Boolean) {
        val silentText = plugin.lang.get("silent")

        val broadcast = (plugin.lang.get("${type.toString().lowercase()}.broadcast") + (if (silent) silentText else ""))
            .toComponent(*resolvers)
        val message = buildMessage()

        plugin.punishmentDatabase.insertPunishment(this)

        if (silent) {
            val possiblePermissions = when (type) {
                Type.WARN -> listOf("warn")
                Type.KICK -> listOf("kick")
                Type.MUTE, Type.UNMUTE -> listOf("mute")
                Type.BAN -> listOf("ban")
                Type.TEMPBAN -> listOf("tempban")
                Type.BANIP -> listOf("banip")
                Type.TEMPBANIP ->listOf("tempbanip")
                Type.UNBAN -> listOf("ban", "tempban")
                Type.UNBANIP -> listOf("banip", "tempbanip")
            }
            plugin.server.onlinePlayers
                .filter { player ->
                    possiblePermissions.any {
                        player.hasPermission(it)
                    }
                }
                .forEach { it.sendMessage(broadcast) }
            plugin.server.consoleSender.sendMessage(broadcast)
        } else {
            plugin.server.sendMessage(broadcast)
        }
        if (player != null && player.isOnline) {
            if (listOf(Type.KICK, Type.BAN, Type.TEMPBAN).contains(type)) {
                player.player!!.kick(message)
            } else if (message != null) {
                player.player!!.sendMessage(message)
            }
        } else ip?.findPlayers()?.forEach { it.kick(message) }
    }
}