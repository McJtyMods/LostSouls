package mcjty.lostsouls.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mcjty.lostsouls.data.LostChunkData;
import mcjty.lostsouls.data.LostSoulData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

public class CommandSetHaunt implements Command<CommandSourceStack> {

    private static final CommandSetHaunt CMD = new CommandSetHaunt();

    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher) {
        return Commands.literal("sethaunt")
                .requires(cs -> cs.hasPermission(1))
                .then(Commands.argument("todo", IntegerArgumentType.integer(0, Integer.MAX_VALUE))
                        .executes(CMD));
    }


    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Integer todo = context.getArgument("todo", Integer.class);
        ServerPlayer player = context.getSource().getPlayerOrException();
        BlockPos position = player.blockPosition();
        LostChunkData soulData = LostSoulData.getSoulData(player.getLevel(), position.getX() >> 4, position.getZ() >> 4, null);
        soulData.setNumberKilled(todo);
        LostSoulData.getData(player.getLevel()).setDirty();
        return 0;
    }
}
