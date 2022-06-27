package mcjty.lostsouls.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mcjty.lostsouls.data.LostChunkData;
import mcjty.lostsouls.data.LostSoulData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

public class CommandDebug implements Command<CommandSourceStack> {

    private static final CommandDebug CMD = new CommandDebug();

    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher) {
        return Commands.literal("debug")
                .requires(cs -> cs.hasPermission(1))
                .executes(CMD);
    }


    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        BlockPos position = player.blockPosition();
        LostChunkData soulData = LostSoulData.getSoulData(player.getLevel(), position.getX() >> 4, position.getZ() >> 4, null);
        System.out.println("soulData.isHaunted() = " + soulData.isHaunted());
        System.out.println("soulData.getMaxMobs() = " + soulData.getTotalMobs());
        System.out.println("soulData.getNumberKilled() = " + soulData.getNumberKilled());
        return 0;
    }
}
