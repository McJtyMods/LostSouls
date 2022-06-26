package mcjty.lostsouls.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import mcjty.lostsouls.LostSouls;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class ModCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralCommandNode<CommandSourceStack> commands = dispatcher.register(
                Commands.literal(LostSouls.MODID)
                        .then(CommandSetHaunt.register(dispatcher))
                        .then(CommandDebug.register(dispatcher))
        );

        dispatcher.register(Commands.literal("ls").redirect(commands));
    }

}
