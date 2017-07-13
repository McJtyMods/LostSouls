package mcjty.lostsouls.data;

import mcjty.lostcities.varia.ChunkCoord;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class LostSoulData extends WorldSavedData {

    public static final String NAME = "LostSoulData";
    private static LostSoulData instance = null;

    private final Map<ChunkCoord, LostChunkData> lostChunkDataMap = new HashMap<>();

    public LostSoulData(String name) {
        super(name);
    }

    public void save(World world) {
        world.setData(NAME, this);
        markDirty();
    }

    public static void clearInstance() {
        if (instance != null) {
            instance.lostChunkDataMap.clear();
            instance = null;
        }
    }

    @Nonnull
    public static LostSoulData getData(World world) {
        if (world.isRemote) {
            throw new RuntimeException("Don't access this client-side!");
        }
        if (instance != null) {
            return instance;
        }
        instance = (LostSoulData) world.loadData(LostSoulData.class, NAME);
        if (instance == null) {
            instance = new LostSoulData(NAME);
        }
        return instance;
    }

    @Nonnull
    public static LostChunkData getSoulData(World world, int dimension, int chunkX, int chunkZ) {
        LostSoulData data = getData(world);
        ChunkCoord cc = new ChunkCoord(dimension, chunkX, chunkZ);
        return data.getSoulData(world, cc);
    }

    private LostChunkData getSoulData(World world, ChunkCoord cc) {
        if (!lostChunkDataMap.containsKey(cc)) {
            lostChunkDataMap.put(cc, new LostChunkData(cc));
            save(world);
        }
        return lostChunkDataMap.get(cc);
    }


    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        NBTTagList list = nbt.getTagList("chunks", Constants.NBT.TAG_COMPOUND);
        for (int i = 0 ; i < list.tagCount() ; i++) {
            NBTTagCompound tc = (NBTTagCompound) list.get(i);
            int dim = tc.getInteger("dim");
            int x = tc.getInteger("x");
            int z = tc.getInteger("z");
            ChunkCoord cc = new ChunkCoord(dim, x, z);
            LostChunkData data = new LostChunkData(cc);
            data.readFromNBT(tc);
            lostChunkDataMap.put(cc, data);
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        NBTTagList list = new NBTTagList();
        for (Map.Entry<ChunkCoord, LostChunkData> entry : lostChunkDataMap.entrySet()) {
            NBTTagCompound tc = new NBTTagCompound();
            tc.setInteger("dim", entry.getKey().getDimension());
            tc.setInteger("x", entry.getKey().getChunkX());
            tc.setInteger("z", entry.getKey().getChunkZ());
            entry.getValue().writeToNBT(tc);
            list.appendTag(tc);
        }
        compound.setTag("chunks", list);
        return compound;
    }
}
