package mcjty.lostsouls;

import mcjty.lostsouls.config.Config;
import mcjty.lostsouls.setup.ModSetup;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
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

        FMLJavaModLoadingContext.get().getModEventBus().addListener(setup::init);

    }

    // @todo 1.18
//    @Mod.EventHandler
//    public void serverStopped(FMLServerStoppedEvent event) {
//        LostSoulData.clearInstance();
//    }
}
