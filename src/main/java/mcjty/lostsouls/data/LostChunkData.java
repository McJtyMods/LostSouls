package mcjty.lostsouls.data;

import mcjty.lostsouls.varia.ChunkCoord;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;

import javax.annotation.Nullable;
import java.util.Random;

public class LostChunkData {

    private boolean haunted;
    private int totalMobs;
    private int numberKilled;
    private int enteredCount;   // Count the number of times a player has entered this building
    private long messagetime;

    // This is not persisted but calculated on demand. It's a cached value
    private MobSettings settings;

    public LostChunkData() {
        numberKilled = 0;
        enteredCount = 0;
        messagetime = -1;
    }

    // The settings given here should be a combination of the global settings and the building settings
    public void initialize(ServerLevel level, ChunkCoord cc, MobSettings settings) {
        Random random = new Random(level.getSeed()*899812591L + cc.chunkX()*916023653L + cc.chunkZ()*797003437L);
        random.nextFloat();
        random.nextFloat();
        haunted = random.nextFloat() < settings.getHauntedChance();
        MobSettings.Range<Integer> amounts = settings.getMobAmounts();
        totalMobs = random.nextInt(amounts.min(), amounts.max());
        this.settings = settings;
    }

    @Nullable
    public MobSettings getSettings() {
        return settings;
    }

    public void setSettings(MobSettings settings) {
        this.settings = settings;
    }

    public boolean isHaunted() {
        return haunted;
    }

    public int getTotalMobs() {
        return totalMobs;
    }

    public int getNumberKilled() {
        return numberKilled;
    }

    public void setNumberKilled(int numberKilled) {
        this.numberKilled = numberKilled;
    }

    public int getEnteredCount() {
        return enteredCount;
    }

    public void newKill() {
        numberKilled++;
    }

    public void enterBuilding() {
        enteredCount++;
    }

    public void setMessagetime(long messagetime) {
        this.messagetime = messagetime;
    }

    public long getMessagetime() {
        return messagetime;
    }

    public void readFromNBT(CompoundTag nbt) {
        haunted = nbt.getBoolean("haunted");
        totalMobs = nbt.getInt("max");
        numberKilled = nbt.getInt("killed");
        enteredCount = nbt.getInt("enteredCount");
        if (nbt.contains("messagetime")) {
            messagetime = nbt.getLong("messagetime");
        } else {
            messagetime = -1;
        }
    }

    public void writeToNBT(CompoundTag compound) {
        compound.putBoolean("haunted", haunted);
        compound.putInt("max", totalMobs);
        compound.putInt("killed", numberKilled);
        compound.putInt("enteredCount", enteredCount);
        compound.putLong("messagetime", messagetime);
    }
}
