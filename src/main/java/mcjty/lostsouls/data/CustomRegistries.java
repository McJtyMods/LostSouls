package mcjty.lostsouls.data;

import mcjty.lostsouls.LostSouls;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CustomRegistries {

    public static final ResourceKey<Registry<MobSettings>> BUILDING_REGISTRY_KEY = ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(LostSouls.MODID, "buildings"));
    public static final DeferredRegister<MobSettings> BUILDING_DEFERRED_REGISTER = DeferredRegister.create(BUILDING_REGISTRY_KEY, LostSouls.MODID);

    public static void init(IEventBus bus) {
        BUILDING_DEFERRED_REGISTER.register(bus);
    }

    public static void onDataPackRegistry(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(BUILDING_REGISTRY_KEY, MobSettings.CODEC);
    }
}
