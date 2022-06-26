package mcjty.lostsouls.setup;

import mcjty.lostcities.api.ILostCities;
import mcjty.lostsouls.ForgeEventHandlers;
import mcjty.lostsouls.config.ConfigSetup;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import javax.annotation.Nullable;
import java.io.File;
import java.util.function.Function;

public class ModSetup {

    public static ILostCities lostCities;

    public static File modConfigDir;
//    private Configuration mainConfig;

    private void setupModCompat() {
//        FMLInterModComms.sendFunctionMessage("lostcities", "getLostCities", "mcjty.lostsouls.setup.ModSetup$GetLostCities");
    }

    public void init(FMLCommonSetupEvent e) {
        MinecraftForge.EVENT_BUS.register(new ForgeEventHandlers());

        setupModCompat();

//        modConfigDir = e.getModConfigurationDirectory();
        ConfigSetup.init();
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
