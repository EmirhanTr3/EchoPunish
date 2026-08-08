package cat.emir.echopunish

import cat.emir.echolib.EchoLang
import cat.emir.echolib.EchoPlugin
import cat.emir.echolib.command.CommandLib
import cat.emir.echolib.event.EventLoader
import cat.emir.echolib.theme.ThemeManager
import cat.emir.echopunish.punishment.PunishmentDatabase
import net.luckperms.api.LuckPerms
import net.luckperms.api.LuckPermsProvider

class EchoPunish : EchoPlugin() {

    companion object {
        lateinit var instance: EchoPunish
            private set
    }

    lateinit var luckPerms: LuckPerms
        private set

    val lang = EchoLang(this, "lang.yml")
    val punishmentDatabase = PunishmentDatabase(this, "punishments")
    val chatContextManager = ChatContextManager(this)

    override fun onEnable() {
        instance = this
        luckPerms = LuckPermsProvider.get()
        lang.load()
        punishmentDatabase.load()

        ThemeManager.builder(this)
            .color("primary", 0x69DDFF)
            .color("secondary", 0x96CDFF)
            .color("success", 0x6EEB83)
            .color("warning", 0xFFCC3F)
            .color("error", 0xF06D78)
            .color("fatal", 0xC2404B)
            .build()

        CommandLib.registerCommands(this, "cat.emir.echopunish.commands")
        EventLoader.registerListeners(this, "cat.emir.echopunish.listeners")
    }

}
