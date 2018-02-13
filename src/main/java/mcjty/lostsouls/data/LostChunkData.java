package mcjty.lostsouls.data;

import mcjty.lostcities.varia.ChunkCoord;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Random;

public class LostChunkData {

    private boolean haunted;
    private int totalMobs;
    private int numberKilled;
    private int enteredCount;   // Count the number of times a player has entered this building

    public LostChunkData(ChunkCoord cc) {
        numberKilled = 0;
        enteredCount = 0;
    }

    public void initialize(ChunkCoord cc, float hauntedChance, int minMobs, int maxMobs) {
        Random random = new Random(cc.getDimension()*899812591L + cc.getChunkX()*916023653L + cc.getChunkZ()*797003437L);
        random.nextFloat();
        random.nextFloat();
        haunted = random.nextFloat() < hauntedChance;
        totalMobs = random.nextInt(maxMobs - minMobs+1) + minMobs;
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

    public void readFromNBT(NBTTagCompound nbt) {
        haunted = nbt.getBoolean("haunted");
        totalMobs = nbt.getInteger("max");
        numberKilled = nbt.getInteger("killed");
        enteredCount = nbt.getInteger("enteredCount");
    }

    public void writeToNBT(NBTTagCompound compound) {
        compound.setBoolean("haunted", haunted);
        compound.setInteger("max", totalMobs);
        compound.setInteger("killed", numberKilled);
        compound.setInteger("enteredCount", enteredCount);
    }
}
