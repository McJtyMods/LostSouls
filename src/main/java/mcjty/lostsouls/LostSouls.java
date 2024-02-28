package mcjty.lostsouls;

import mcjty.lostsouls.setup.Config;
import mcjty.lostsouls.setup.ModSetup;
import net.neoforged.neoforge.fml.ModLoadingContext;
import net.neoforged.neoforge.fml.common.Mod;
import net.neoforged.neoforge.fml.config.ModConfig;
import net.neoforged.neoforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(LostSouls.MODID)
public class LostSouls {
    public static final String MODID = "lostsouls";

    public static Logger logger = LogManager.getLogger(LostSouls.MODID);
    public static ModSetup setup = new ModSetup();

    public static LostSouls instance;

    public LostSouls() {
        instance = this;
        Config.register();
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.SERVER_CONFIG);

        FMLJavaModLoadingContext.get().getModEventBus().addListener(setup::init);
    }
}
