package mcjty.lostsouls.commands;

import mcjty.lostsouls.data.LostChunkData;
import mcjty.lostsouls.data.LostSoulData;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class CommandDebug implements ICommand {

    @Override
    public String getName() {
        return "ls_debug";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return getName();
    }

    @Override
    public List<String> getAliases() {
        return Collections.emptyList();
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (sender instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) sender;
            BlockPos position = player.getPosition();
            LostChunkData soulData = LostSoulData.getSoulData(player.getEntityWorld(), player.getEntityWorld().provider.getDimension(), position.getX() >> 4, position.getZ() >> 4);
            System.out.println("soulData.isHaunted() = " + soulData.isHaunted());
            System.out.println("soulData.getMaxMobs() = " + soulData.getMaxMobs());
            System.out.println("soulData.getNumberKilled() = " + soulData.getNumberKilled());
        }
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return true;
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        return Collections.emptyList();
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index) {
        return false;
    }

    @Override
    public int compareTo(ICommand o) {
        return getName().compareTo(o.getName());
    }
}
