package cat.emir.echopunish.uis

import cat.emir.echolib.extensions.getTimeAgo
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

import com.github.stefvanschie.inventoryframework.adventuresupport.ComponentHolder
import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane
import com.github.stefvanschie.inventoryframework.pane.StaticPane
import com.github.stefvanschie.inventoryframework.pane.util.Slot

import net.kyori.adventure.text.format.TextDecoration
import cat.emir.echolib.extensions.toComponent
import cat.emir.echolib.extensions.toReadableString
import cat.emir.echopunish.EchoPunish
import cat.emir.echopunish.nameOrUniqueId
import cat.emir.echopunish.punishment.Punishment.Type
import com.github.stefvanschie.inventoryframework.pane.Pane
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class HistoryGUI(val plugin: EchoPunish, val viewer: Player, val target: OfflinePlayer) {
    fun openGUI() {
        val gui = ChestGui(6, ComponentHolder.of("<aqua>History of ${target.nameOrUniqueId}".toComponent()))
        val outerPane = StaticPane(9, 6)

        val borderItem = ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE).apply {
            editMeta {
                it.isHideTooltip = true
            }
        }

        listOf(
            0, 1, 2, 3, 4, 5, 6, 7, 8,
            9, 17,
            18, 26,
            27, 35,
            36, 44,
            45, 46, 47, 48, 49, 50, 51, 52, 53
        ).forEach {
            outerPane.addItem(GuiItem(borderItem), Slot.fromIndex(it))
        }

        val innerPane = PaginatedPane(7, 4)

        val dateTimeFormatter = DateTimeFormatter
            .ofLocalizedDateTime(FormatStyle.MEDIUM)
            .withZone(ZoneOffset.UTC)
            .withLocale(viewer.locale())

        val types = listOf("All", "Ban", "IP Ban", "Tempban", "IP Tempban", "Mute", "Kick", "Warn")
        var currentTypeIndex = 0

        fun drawItems(initial: Boolean) {
            if (!initial) {
                if (innerPane.pages > 1)
                    (1..<innerPane.pages).forEach { innerPane.deletePage(it) }
                innerPane.panes.forEach { it.items.clear() }
                innerPane.page = 0
            }

            val typesToRender = when (types[currentTypeIndex]) {
                "Ban" -> listOf(Type.BAN, Type.UNBAN)
                "IP Ban" -> listOf(Type.BANIP, Type.UNBANIP)
                "Tempban" -> listOf(Type.TEMPBAN, Type.UNBAN)
                "IP Tempban" -> listOf(Type.TEMPBANIP, Type.UNBANIP)
                "Mute" -> listOf(Type.MUTE, Type.UNMUTE)
                "Kick" -> listOf(Type.KICK)
                "Warn" -> listOf(Type.WARN)
                else -> Type.entries.toList()
            }

            val prefixGuiItems = plugin.punishmentDatabase.getAllPunishments(target.uniqueId).values
                .filter { typesToRender.contains(it.type) }
                .sortedByDescending { it.issuedAt }
                .map { punishment ->
                    val material = when (punishment.type) {
                        Type.WARN -> Material.CYAN_DYE
                        Type.KICK -> Material.GREEN_DYE
                        Type.MUTE -> Material.ORANGE_DYE
                        Type.BAN -> Material.PURPLE_DYE
                        Type.TEMPBAN -> Material.RED_DYE
                        Type.BANIP -> Material.BLACK_DYE
                        Type.TEMPBANIP -> Material.GRAY_DYE
                        Type.UNMUTE -> Material.YELLOW_DYE
                        Type.UNBAN -> Material.MAGENTA_DYE
                        Type.UNBANIP -> Material.LIGHT_GRAY_DYE
                    }

                    val name = when (punishment.type) {
                        Type.WARN -> "<dark_aqua>Warn"
                        Type.KICK -> "<dark_green>Kick"
                        Type.MUTE -> "<gold>Mute"
                        Type.BAN -> "<light_purple>Ban"
                        Type.TEMPBAN -> "<red>Tempban"
                        Type.BANIP -> "<dark_gray>IP Ban"
                        Type.TEMPBANIP -> "<gray>IP Tempban"
                        Type.UNMUTE -> "<yellow>Unmute"
                        Type.UNBAN -> "<#ffaaff>Unban"
                        Type.UNBANIP -> "<gray>IP Unban"
                    }

                    val currentMute = plugin.punishmentDatabase.getCurrentMute(target.uniqueId)
                    val currentBan = plugin.punishmentDatabase.getCurrentBan(target.uniqueId)

                    val item = ItemStack(material).apply {
                        editMeta { meta ->
                            val active = (currentMute != null && punishment.id == currentMute.id) ||
                                    (currentBan != null && punishment.id == currentBan.id)

                            meta.setEnchantmentGlintOverride(active)

                            meta.customName(
                                (if (active) "$name <gray>(<green>Active</green>)</gray>" else name).toComponent()
                                    .decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                            )

                            meta.lore(
                                listOfNotNull(
                                    "<primary>Mod:</primary> <secondary>${punishment.mod?.name ?: punishment.modUuid ?: "Server"}</secondary>",
                                    if (punishment.duration != null)
                                        "<primary>Duration:</primary> <secondary>${
                                            punishment.duration.toReadableString(
                                                false,
                                                ", "
                                            )
                                        }</secondary>"
                                    else null,
                                    "<primary>Reason:</primary> <secondary>${punishment.reason}",
                                    "<primary>Issued At:</primary> <secondary>${dateTimeFormatter.format(punishment.issuedAt)} <primary>(</primary>${punishment.issuedAt.getTimeAgo()}<primary>)</primary></secondary>",
                                    "",
                                    if (!punishment.chatContext.isNullOrEmpty())
                                        "<gray>Has chat context (${punishment.chatContext.size})"
                                    else null,
                                    "<dark_gray>ID: ${punishment.id}"
                                )
                                    .map { it.toComponent() }
                                    .map { it.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE) }
                            )
                        }
                    }

                    GuiItem(item).apply {
                        setAction {
                            PunishmentInfoDialog(plugin, viewer, punishment).showDialog()
                        }
                    }
                }

            innerPane.populateWithGuiItems(prefixGuiItems)

            if (!initial) gui.update()
        }

        drawItems(true)

        var lastTypeChanceTime = 0L
        val navigationPane = StaticPane(9, 1, Pane.Priority.HIGH)

        fun drawPagingButtons(initial: Boolean) {
            navigationPane.items.clear()

            if (innerPane.page > 0) {
                val previousItem = GuiItem(ItemStack(Material.ARROW).apply {
                    editMeta { meta ->
                        meta.customName(
                            "<primary>Previous Page</primary>".toComponent()
                                .decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                        )
                    }
                }).apply {
                    setAction {
                        innerPane.page -= 1

                        drawPagingButtons(false)
                    }
                }
                navigationPane.addItem(previousItem, 3, 0)
            }

            if (innerPane.pages > 1) {
                val currentItem = GuiItem(ItemStack(Material.BOOK).apply {
                    editMeta { meta ->
                        meta.customName(
                            "<primary>Page <secondary>${innerPane.page + 1}</secondary> / <secondary>${innerPane.pages}</secondary></primary>".toComponent()
                                .decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                        )
                    }
                })
                navigationPane.addItem(currentItem, 4, 0)
            }

            if (innerPane.page < innerPane.pages - 1) {
                val nextItem = GuiItem(ItemStack(Material.ARROW).apply {
                    editMeta { meta ->
                        meta.customName(
                            "<primary>Next Page</primary>".toComponent()
                                .decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                        )
                    }
                }).apply {
                    setAction {
                        innerPane.page += 1

                        drawPagingButtons(false)
                    }
                }
                navigationPane.addItem(nextItem, 5, 0)
            }

            val typeFilterItem = GuiItem(ItemStack(Material.HOPPER).apply {
                editMeta { meta ->
                    meta.customName("<primary>Type Filter</primary>".toComponent()
                        .decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE))

                    meta.lore(types
                        .map {
                            val currentType = types[currentTypeIndex]
                            if (currentType == it) {
                                "<success>> $it</success>".toComponent()
                            } else {
                                "<gray>> $it</gray>".toComponent()
                            }
                        }
                        .map { it.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE) }
                    )
                }
            }).apply {
                setAction {
                    if ((System.currentTimeMillis() - lastTypeChanceTime) < 200) return@setAction
                    lastTypeChanceTime = System.currentTimeMillis()

                    if (it.isLeftClick) {
                        currentTypeIndex += 1
                        if (currentTypeIndex > types.size - 1) {
                            currentTypeIndex = 0
                        }
                    } else if (it.isRightClick) {
                        currentTypeIndex -= 1
                        if (currentTypeIndex < 0) {
                            currentTypeIndex = types.size - 1
                        }
                    }

                    drawItems(false)
                    drawPagingButtons(false)
                }
            }
            navigationPane.addItem(typeFilterItem, 7, 0)

            if (!initial) gui.update()
        }

        drawPagingButtons(true)

        gui.addPane(Slot.fromIndex(0), outerPane)
        gui.addPane(Slot.fromXY(1, 1), innerPane)
        gui.addPane(Slot.fromXY(0, 5), navigationPane)

        gui.setOnGlobalClick {
            it.isCancelled = true
        }

        gui.show(viewer)
    }
}
