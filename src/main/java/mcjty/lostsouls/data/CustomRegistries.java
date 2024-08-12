package mcjty.lostsouls.data;

import mcjty.lostsouls.LostSouls;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

import java.util.function.Supplier;

public class CustomRegistries {

    public static final ResourceKey<Registry<MobSettings>> BUILDING_REGISTRY_KEY = ResourceKey.createRegistryKey(new ResourceLocation(LostSouls.MODID, "buildings"));
    public static final DeferredRegister<MobSettings> BUILDING_DEFERRED_REGISTER = DeferredRegister.create(BUILDING_REGISTRY_KEY, LostSouls.MODID);
    public static final Supplier<IForgeRegistry<MobSettings>> BUILDING_REGISTRY = BUILDING_DEFERRED_REGISTER.makeRegistry(() -> new RegistryBuilder<MobSettings>().dataPackRegistry(MobSettings.CODEC));

    public static void init() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        BUILDING_DEFERRED_REGISTER.register(bus);
    }
}
