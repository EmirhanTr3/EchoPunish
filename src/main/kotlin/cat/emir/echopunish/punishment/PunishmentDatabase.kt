package cat.emir.echopunish.punishment

import cat.emir.echolib.database.ExposedFileDatabase
import cat.emir.echopunish.EchoPunish
import cat.emir.echopunish.punishment.Punishment.Type
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.alias
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.greater
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.core.isNull
import org.jetbrains.exposed.v1.core.notExists
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.core.plus
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.net.InetSocketAddress
import java.time.Duration
import java.time.Instant
import java.util.UUID

class PunishmentDatabase(val plugin: EchoPunish, name: String) : ExposedFileDatabase(plugin, name) {
    private val punishments = mutableMapOf<String, ReadOnlyPunishment>()

    object PunishmentsTable: Table("punishments") {
        val id = varchar("id", 15)
        val type = varchar("type", 32)
        val uuid = varchar("uuid", 36).nullable()
        val modUuid = varchar("mod_uuid", 36).nullable()
        val duration = long("duration").nullable()
        val issuedAt = long("issued_at")
        val reason = text("reason")
        val chatContext = text("chat_context").nullable()
        val ip = text("ip").nullable()

        override val primaryKey = PrimaryKey(id)
    }

    override fun load() {
        transaction {
            SchemaUtils.createMissingTablesAndColumns(PunishmentsTable)

            PunishmentsTable.selectAll().toList()
                .mapNotNull { rowToPunishment(it) }
                .forEach { punishments[it.id] = it }
        }
    }

    fun insertPunishment(punishment: Punishment) {
        transaction {
            PunishmentsTable.insert {
                it[id] = punishment.id
                it[type] = punishment.type.name
                it[uuid] = punishment.uuid?.toString()
                it[ip] = punishment.ip?.hostString
                it[modUuid] = punishment.modUuid?.toString()
                it[duration] = punishment.duration?.toMillis()
                it[issuedAt] = punishment.issuedAt.toEpochMilli()
                it[reason] = punishment.reason
                it[chatContext] = punishment.chatContext?.let { pChatContext -> Gson().toJson(pChatContext) }
            }

            punishments[punishment.id] = makeReadOnly(punishment)
        }
    }

    private fun makeReadOnly(punishment: Punishment): ReadOnlyPunishment {
        return ReadOnlyPunishment(
            id = punishment.id,
            type = punishment.type,
            uuid = punishment.uuid,
            ip = punishment.ip,
            modUuid = punishment.modUuid,
            duration = punishment.duration,
            issuedAt = punishment.issuedAt,
            reason = punishment.reason,
            chatContext = punishment.chatContext
        )
    }

    private fun rowToPunishment(row: ResultRow?): ReadOnlyPunishment? {
        if (row == null) return null
        println(row[PunishmentsTable.chatContext])
        return ReadOnlyPunishment(
            id = row[PunishmentsTable.id],
            type = Type.valueOf(row[PunishmentsTable.type]),
            uuid = row[PunishmentsTable.uuid]?.let { UUID.fromString(it) },
            ip = row[PunishmentsTable.ip]?.let { InetSocketAddress(it, 0) },
            modUuid = row[PunishmentsTable.modUuid]?.let { UUID.fromString(it) },
            duration = row[PunishmentsTable.duration]?.let { Duration.ofMillis(it) },
            issuedAt = Instant.ofEpochMilli(row[PunishmentsTable.issuedAt]),
            reason = row[PunishmentsTable.reason],
            chatContext = row[PunishmentsTable.chatContext]?.let { Gson().fromJson(it, object : TypeToken<List<String>>(){}.type) },
        )
    }

    fun getPunishment(id: String): ReadOnlyPunishment? {
        if (punishments[id] != null) return punishments[id]
        val row = transaction { PunishmentsTable.selectAll().where(PunishmentsTable.id eq id).firstOrNull() } ?: return null
        val punishment = rowToPunishment(row)
        punishment?.let { punishments[punishment.id] = punishment }
        return punishment
    }

    fun getAllPunishments(): Map<String, ReadOnlyPunishment> {
        return punishments
    }

    fun getAllPunishments(uuid: UUID): Map<String, ReadOnlyPunishment> {
        return punishments
            .filter { it.value.uuid == uuid }
    }

    @Suppress("UNCHECKED_CAST")
    private fun getCurrentPunishmentN(
        column: Column<String>,
        value: String,
        types: List<String>,
        reverseType: String,
        now: Long = System.currentTimeMillis()
    ): ResultRow? {
        return getCurrentPunishment(column as Column<String?>, value, types, reverseType, now)
    }

    private fun getCurrentPunishment(
        column: Column<String?>,
        value: String,
        types: List<String>,
        reverseType: String,
        now: Long = System.currentTimeMillis()
    ): ResultRow? {
        val p2 = PunishmentsTable.alias("p2")

        return transaction {
            PunishmentsTable.selectAll()
                .where {
                    (column eq value) and
                            (PunishmentsTable.type inList types) and
                            (PunishmentsTable.duration.isNull() or
                                    ((PunishmentsTable.duration + PunishmentsTable.issuedAt) greater now)) and
                            notExists(
                                p2.selectAll().where {
                                    (p2[column] eq column) and
                                            (p2[PunishmentsTable.type] eq reverseType) and
                                            (p2[PunishmentsTable.issuedAt] greater PunishmentsTable.issuedAt)
                                }
                            )
                }
                .orderBy(PunishmentsTable.issuedAt, SortOrder.DESC)
                .limit(1)
                .firstOrNull()
        }
    }

    fun getCurrentMute(uuid: UUID): ReadOnlyPunishment? = rowToPunishment(
        getCurrentPunishment(PunishmentsTable.uuid, uuid.toString(), listOf("MUTE"), "UNMUTE")
    )

    fun getCurrentBan(uuid: UUID): ReadOnlyPunishment? = rowToPunishment(
        getCurrentPunishment(PunishmentsTable.uuid, uuid.toString(), listOf("BAN", "TEMPBAN"), "UNBAN")
    )

    fun getCurrentIpBan(ip: String): ReadOnlyPunishment? = rowToPunishment(
        getCurrentPunishment(PunishmentsTable.ip, ip, listOf("BANIP", "TEMPBANIP"), "UNBANIP")
    )
}