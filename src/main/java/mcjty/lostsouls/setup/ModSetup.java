package mcjty.lostsouls.setup;

import mcjty.lostcities.api.ILostCities;
import mcjty.lostsouls.ForgeEventHandlers;
import mcjty.lostsouls.config.ConfigSetup;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.File;
import java.util.function.Function;

public class ModSetup {

    public static ILostCities lostCities;

    private static Logger logger;
    public static File modConfigDir;
    private Configuration mainConfig;

    public void preInit(FMLPreInitializationEvent e) {
        logger = e.getModLog();

        MinecraftForge.EVENT_BUS.register(new ForgeEventHandlers());

        setupModCompat();

        modConfigDir = e.getModConfigurationDirectory();
        ConfigSetup.init();
    }

    private void setupModCompat() {
        FMLInterModComms.sendFunctionMessage("lostcities", "getLostCities", "mcjty.lostsouls.setup.ModSetup$GetLostCities");
    }

    public static Logger getLogger() {
        return logger;
    }

    public void init(FMLInitializationEvent e) {
    }

    public void postInit(FMLPostInitializationEvent e) {
        ConfigSetup.postInit();
    }

    public static class GetLostCities implements Function<ILostCities, Void> {
        @Nullable
        @Override
        public Void apply(ILostCities lc) {
            lostCities = lc;
            return null;
        }
    }

}
