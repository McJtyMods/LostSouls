package mcjty.lostsouls.data;

import mcjty.lostcities.api.ILostChunkInfo;
import mcjty.lostcities.api.ILostCityInformation;
import mcjty.lostcities.api.ILostSphere;
import mcjty.lostsouls.LostSouls;
import mcjty.lostsouls.setup.Config;
import mcjty.lostsouls.varia.ChunkCoord;
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

    // Cache for building settings from the registry
    private Map<ResourceLocation, MobSettings> buildingSettings = null;
    private Map<ResourceLocation, MobSettings> multiBuildingSettings = null;

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

    private void calculateSettingCache(ServerLevel level) {
        if (buildingSettings == null) {
            buildingSettings = new HashMap<>();
            multiBuildingSettings = new HashMap<>();
            Registry<MobSettings> registry = level.registryAccess().registryOrThrow(CustomRegistries.BUILDING_REGISTRY_KEY);
            for (MobSettings r : registry) {
                r.getBuildings().forEach(b -> buildingSettings.put(b, r));
                r.getMultiBuildings().forEach(b -> multiBuildingSettings.put(b, r));
            }
        }
    }

    private MobSettings getSettingsForBuilding(ServerLevel level, ResourceLocation building) {
        calculateSettingCache(level);
        return buildingSettings.get(building);
    }

    private MobSettings getSettingsForMultiBuilding(ServerLevel level, ResourceLocation building) {
        calculateSettingCache(level);
        return multiBuildingSettings.get(building);
    }

    @Nonnull
    public MobSettings getSettingsForChunk(ServerLevel world, ChunkCoord cc, @Nullable ILostCityInformation lost) {
        if (lost == null) {
            return Config.getDefaultSettings();
        } else {
            ILostSphere sphere = lost.getSphere(cc.chunkX() << 4, cc.chunkZ() << 4);
            if (sphere != null) {
                return Config.getDefaultSphereSettings();
            }
            ILostChunkInfo chunkInfo = lost.getChunkInfo(cc.chunkX(), cc.chunkZ());
            ILostChunkInfo.MultiBuildingInfo mb = chunkInfo.getMultiBuildingInfo();
            if (mb == null) {
                ResourceLocation buildingType = chunkInfo.getBuildingId();
                MobSettings mobSettings = getSettingsForBuilding(world, buildingType);
                if (mobSettings == null) {
                    mobSettings = Config.getDefaultSettings();
                } else {
                    mobSettings = MobSettings.merge(Config.getDefaultSettings(), mobSettings);
                }
                return mobSettings;
            } else {
                float chunks = mb.w() * mb.h();
                MobSettings defaultMultiSettings = Config.getDefaultMultiSettings(chunks);
                MobSettings mobSettings = getSettingsForMultiBuilding(world, mb.buildingType());
                if (mobSettings == null) {
                    mobSettings = defaultMultiSettings;
                } else {
                    mobSettings = MobSettings.merge(defaultMultiSettings, mobSettings);
                }
                return mobSettings;
            }
        }
    }

    private LostChunkData getSoulData(ServerLevel world, ChunkCoord cc, @Nullable ILostCityInformation lost) {
        // Multichunk building check

        if (lost != null) {
            ILostChunkInfo chunkInfo = lost.getChunkInfo(cc.chunkX(), cc.chunkZ());
            ILostChunkInfo.MultiBuildingInfo mb = chunkInfo.getMultiBuildingInfo();
            if (mb != null) {
                LostChunkData data = new LostChunkData();
                ChunkCoord topleft = cc.offset(-mb.offsetX(), -mb.offsetZ());
                if (!lostChunkDataMap.containsKey(topleft)) {
                    MobSettings settings = getSettingsForChunk(world, topleft, lost);
                    data.initialize(world, topleft, settings);
                    lostChunkDataMap.put(topleft, data);
                    setDirty();
                    return lostChunkDataMap.get(topleft);
                }
                else {
                    return lostChunkDataMap.get(topleft);
                }
            }
        }

        // Single chunk building check
        if (!lostChunkDataMap.containsKey(cc)) {
            LostChunkData data = new LostChunkData();
            if (lost == null) {
                data.initialize(world, cc, Config.getDefaultSettings());
                lostChunkDataMap.put(cc, data);
            } else {
                MobSettings settings = getSettingsForChunk(world, cc, lost);
                data.initialize(world, cc, settings);
                lostChunkDataMap.put(cc, data);
            }
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
            LostChunkData data = new LostChunkData();
            data.readFromNBT(tc);
            lostChunkDataMap.put(new ChunkCoord(dim, x, z), data);
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
