package mcjty.lostsouls.setup;

import mcjty.lostcities.api.ILostCities;
import mcjty.lostsouls.ForgeEventHandlers;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.fml.InterModComms;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

import javax.annotation.Nullable;
import java.util.function.Function;

public class ModSetup {

    public static ILostCities lostCities;

    public void init(FMLCommonSetupEvent e) {
        NeoForge.EVENT_BUS.register(new ForgeEventHandlers());
        InterModComms.sendTo(ILostCities.LOSTCITIES, ILostCities.GET_LOST_CITIES, ModSetup.GetLostCities::new);
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
