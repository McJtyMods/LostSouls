package mcjty.lostsouls.data;

import mcjty.lostcities.api.ILostChunkInfo;
import mcjty.lostcities.api.ILostCityInformation;
import mcjty.lostcities.varia.ChunkCoord;
import mcjty.lostsouls.setup.Config;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class LostSoulData extends SavedData {

    public static final String NAME = "LostSoulData";

    private final Map<ChunkCoord, LostChunkData> lostChunkDataMap = new HashMap<>();

    @Nonnull
    public static LostSoulData getData(Level world) {
        if (world.isClientSide) {
            throw new RuntimeException("Don't access this client-side!");
        }
        DimensionDataStorage storage = ((ServerLevel)world).getDataStorage();
        return storage.computeIfAbsent(LostSoulData::new, LostSoulData::new, NAME);
    }

    public LostSoulData() {
    }

    public LostSoulData(CompoundTag tag) {
        load(tag);
    }


    @Nonnull
    public static LostChunkData getSoulData(Level world, int chunkX, int chunkZ, @Nullable ILostCityInformation lost) {
        LostSoulData data = getData(world);
        ChunkCoord cc = new ChunkCoord(world.dimension(), chunkX, chunkZ);
        return data.getSoulData((ServerLevel) world, cc, lost);
    }

    private LostChunkData getSoulData(ServerLevel world, ChunkCoord cc, @Nullable ILostCityInformation lost) {
        if (!lostChunkDataMap.containsKey(cc)) {
            LostChunkData data = new LostChunkData(cc);
            if (lost == null) {
                data.initialize(world, cc, Config.HAUNTED_CHANCE.get(), Config.MIN_MOBS.get(), Config.MAX_MOBS.get());
            } else {
                ILostChunkInfo info = lost.getChunkInfo(cc.chunkX(), cc.chunkZ());
                if (info.getSphere() != null) {
                    data.initialize(world, cc, Config.SPHERE_HAUNTED_CHANCE.get(), Config.SPHERE_MIN_MOBS.get(), Config.SPHERE_MAX_MOBS.get());
                } else {
                    data.initialize(world, cc, Config.HAUNTED_CHANCE.get(), Config.MIN_MOBS.get(), Config.MAX_MOBS.get());
                }
            }
            lostChunkDataMap.put(cc, data);
            setDirty();
        }
        return lostChunkDataMap.get(cc);
    }


    private void load(CompoundTag nbt) {
        ListTag list = nbt.getList("chunks", Tag.TAG_COMPOUND);
        for (Tag tag : list) {
            CompoundTag tc = (CompoundTag) tag;
            ResourceKey<Level> dim = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(tc.getString("dim")));
            int x = tc.getInt("x");
            int z = tc.getInt("z");
            ChunkCoord cc = new ChunkCoord(dim, x, z);
            LostChunkData data = new LostChunkData(cc);
            data.readFromNBT(tc);
            lostChunkDataMap.put(cc, data);
        }
    }

    @Override
    public CompoundTag save(CompoundTag compound) {
        ListTag list = new ListTag();
        for (Map.Entry<ChunkCoord, LostChunkData> entry : lostChunkDataMap.entrySet()) {
            CompoundTag tc = new CompoundTag();
            tc.putString("dim", entry.getKey().dimension().location().toString());
            tc.putInt("x", entry.getKey().chunkX());
            tc.putInt("z", entry.getKey().chunkZ());
            entry.getValue().writeToNBT(tc);
            list.add(tc);
        }
        compound.put("chunks", list);
        return compound;
    }
}
