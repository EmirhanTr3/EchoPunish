package cat.emir.echopunish.arguments

import cat.emir.echolib.command.CommandLib
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands

fun CommandLib.CommandNode.punishmentReasonArgument(
    callback: (CommandContext<CommandSourceStack>, Boolean, String) -> Int,
    defaultReason: String
) {
    node.then(Commands.literal("-s")
        .then(Commands.argument("reason", StringArgumentType.greedyString())
            .executes { ctx -> callback(ctx, true, StringArgumentType.getString(ctx, "reason")) }
        )
        .executes { ctx -> callback(ctx, true, defaultReason) }

    ).then(Commands.argument("reason", StringArgumentType.greedyString())
        .executes { ctx -> callback(ctx, false, StringArgumentType.getString(ctx, "reason")) }
    )
    .executes { ctx -> callback(ctx, false, defaultReason) }
}
