package cat.emir.echopunish

import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import java.util.UUID

class ChatContextManager(val plugin: EchoPunish) {

    data class ChatContextMessage(val uuid: UUID, val message: String)

    val chatMessages = mutableListOf<ChatContextMessage>()

    fun addMessage(player: Player, message: String) {
        chatMessages.add(ChatContextMessage(player.uniqueId, message))
        if (chatMessages.size > 100) {
            chatMessages.removeFirst()
        }
    }

    fun createContext(player: OfflinePlayer): List<String> {
        val lastMessage = chatMessages.findLast { it.uuid == player.uniqueId }
        if (lastMessage == null) return emptyList()
        val index = chatMessages.indexOf(lastMessage)

        val windowSize = 20
        var start = index - windowSize / 2
        var end = start + windowSize - 1

        if (start < 0) {
            end -= start
            start = 0
        }
        if (end > chatMessages.lastIndex) {
            start -= (end - chatMessages.lastIndex)
            end = chatMessages.lastIndex
        }
        start = start.coerceAtLeast(0)

        return chatMessages.subList(start, end + 1)
            .map { it.message }
    }
}