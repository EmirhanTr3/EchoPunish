package cat.emir.echopunish.uis

import cat.emir.echolib.extensions.getTimeAgo
import cat.emir.echolib.extensions.toComponent
import cat.emir.echolib.extensions.toReadableString
import cat.emir.echopunish.EchoPunish
import cat.emir.echopunish.punishment.ReadOnlyPunishment
import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.data.dialog.ActionButton
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.body.DialogBody
import io.papermc.paper.registry.data.dialog.type.DialogType
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.entity.Player
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Suppress("UnstableApiUsage")
class PunishmentInfoDialog(val plugin: EchoPunish, val viewer: Player, val punishment: ReadOnlyPunishment) {
    fun showDialog() {
        val dateTimeFormatter = DateTimeFormatter
            .ofLocalizedDateTime(FormatStyle.MEDIUM)
            .withZone(ZoneOffset.UTC)
            .withLocale(viewer.locale())

        val dialog = Dialog.create { builder -> builder.empty()
            .base(DialogBase.builder("<primary>Punishment information of <secondary>${punishment.id}</secondary></primary>".toComponent())
                .body(listOf(
                    DialogBody.plainMessage(
                        listOfNotNull(
                            "<primary>Type:</primary> <secondary>${punishment.type}</secondary>",
                            if (punishment.player != null || punishment.uuid != null)
                                "<primary>Player:</primary> <secondary>${punishment.player?.name ?: punishment.uuid}</secondary>"
                            else if (punishment.ip != null)
                                "<primary>IP:</primary> <secondary>${punishment.ip}</secondary>"
                            else null,
                            "<primary>Mod:</primary> <secondary>${punishment.mod?.name ?: punishment.modUuid ?: "Server"}</secondary>",
                            if (punishment.duration != null)
                                "<primary>Duration:</primary> <secondary>${punishment.duration.toReadableString(false, ", ")}</secondary>"
                            else null,
                            "<primary>Reason:</primary> <secondary>${punishment.reason}",
                            "<primary>Issued At:</primary> <secondary>${dateTimeFormatter.format(punishment.issuedAt)} <primary>(</primary>${punishment.issuedAt.getTimeAgo()}<primary>)</primary></secondary>",
                            "",
                            if (!punishment.chatContext.isNullOrEmpty())
                                "<primary><bold>Chat Context:</bold>\n" +
                                "<reset><white><chatcontext>"
                            else null
                        )
                        .joinToString("\n")
                        .toComponent(
                            Placeholder.component("chatcontext",
                                if (!punishment.chatContext.isNullOrEmpty())
                                    punishment.chatContext
                                        .joinToString("\n")
                                        .toComponent()
                                else "null".toComponent()
                            )
                        ), 500
                    )
                ))
                .build()
            )
            .type(DialogType.notice(
                ActionButton.builder("Close".toComponent())
                    .width(200)
                    .build()
            ))
        }

        viewer.showDialog(dialog)
    }
}