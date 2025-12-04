package mcjty.lostsouls;

import mcjty.lostsouls.data.CustomRegistries;
import mcjty.lostsouls.setup.Config;
import mcjty.lostsouls.setup.ModSetup;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

@Mod(LostSouls.MODID)
public class LostSouls {
    public static final String MODID = "lostsouls";

    public static Logger logger = LogManager.getLogger(LostSouls.MODID);
    public static ModSetup setup = new ModSetup();

    public static LostSouls instance;

    public LostSouls(ModContainer mod, IEventBus bus, Dist dist) {
        instance = this;
        Config.register();
        mod.registerConfig(ModConfig.Type.SERVER, Config.SERVER_CONFIG);
        CustomRegistries.init(bus);

        bus.addListener(setup::init);
        bus.addListener(CustomRegistries::onDataPackRegistry);
    }
}
