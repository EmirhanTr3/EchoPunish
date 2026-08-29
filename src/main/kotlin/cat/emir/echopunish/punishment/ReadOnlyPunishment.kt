package cat.emir.echopunish.punishment

import java.net.InetSocketAddress
import java.time.Duration
import java.time.Instant
import java.util.UUID

class ReadOnlyPunishment(
    id: String,
    type: Type,
    uuid: UUID?,
    ip: InetSocketAddress?,
    modUuid: UUID?,
    duration: Duration?,
    issuedAt: Instant,
    reason: String,
    chatContext: List<String>?,
    targetPunishmentId: String?
) : Punishment(id, type, uuid, ip, modUuid, duration, issuedAt, reason, chatContext, targetPunishmentId) {

    @Deprecated("this is a read only punishment and cannot be executed, will throw an error")
    override fun execute(silent: Boolean) {
        throw UnsupportedOperationException("ReadOnlyPunishments cannot be executed.")
    }
}