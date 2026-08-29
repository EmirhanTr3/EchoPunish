package cat.emir.echopunish.utils

object MiniMessageUtils {
    val colorAndFormatMap = mapOf(
        '0' to "black",      '1' to "dark_blue",    '2' to "dark_green",    '3' to "dark_aqua",
        '4' to "dark_red",   '5' to "dark_purple",  '6' to "gold",          '7' to "gray",
        '8' to "dark_gray",  '9' to "blue",         'a' to "green",         'b' to "aqua",
        'c' to "red",        'd' to "light_purple", 'e' to "yellow",        'f' to "white",
        'k' to "obfuscated", 'l' to "bold",         'm' to "strikethrough", 'n' to "underlined",
        'o' to "italic",     'r' to "reset"
    )

    fun convertColorToMiniMessage(text: String): String {
        var result = text

        val vanillaHexRegex = Regex("[&§]x([&§][0-9a-fA-F]){6}")
        result = vanillaHexRegex.replace(result) { match ->
            val hex = match.value.replace(Regex("[&§]x"), "").replace(Regex("[&§]"), "")
            "<#$hex>"
        }

        val hexRegex = Regex("[&§]#([0-9a-fA-F]{6})")
        result = hexRegex.replace(result) { match ->
            "<#${match.groupValues[1]}>"
        }

        val regex = Regex("[&§]([0-9a-fk-or])", RegexOption.IGNORE_CASE)
        result = regex.replace(result) { match ->
            val code = match.groupValues[1].lowercase()[0]
            val tag = colorAndFormatMap[code]
            if (tag != null) "<$tag>" else match.value
        }

        return result
    }
}