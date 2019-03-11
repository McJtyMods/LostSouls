package mcjty.lostsouls;

import mcjty.lostsouls.commands.CommandDebug;
import mcjty.lostsouls.commands.CommandSetHaunt;
import mcjty.lostsouls.data.LostSoulData;
import mcjty.lostsouls.setup.IProxy;
import mcjty.lostsouls.setup.ModSetup;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;

@Mod(modid = LostSouls.MODID, name = "Lost Souls",
        dependencies =
                "required-after:lostcities@[" + LostSouls.LOSTCITY_VERSION + ",);" +
                        "after:forge@[" + LostSouls.MIN_FORGE11_VER + ",)",
        version = LostSouls.VERSION,
        acceptedMinecraftVersions = "[1.12,1.13)",
        acceptableRemoteVersions = "*")
public class LostSouls {
    public static final String MODID = "lostsouls";
    public static final String VERSION = "1.1.4";
    public static final String LOSTCITY_VERSION = "2.0.3";
    public static final String MIN_FORGE11_VER = "13.19.0.2176";

    @SidedProxy(clientSide = "mcjty.lostsouls.setup.ClientProxy", serverSide = "mcjty.lostsouls.setup.ServerProxy")
    public static IProxy proxy;
    public static ModSetup setup = new ModSetup();

    @Mod.Instance("lostsouls")
    public static LostSouls instance;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent e) {
        setup.preInit(e);
        proxy.preInit(e);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent e) {
        setup.init(e);
        proxy.init(e);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent e) {
        setup.postInit(e);
        proxy.postInit(e);
    }

    @Mod.EventHandler
    public void serverLoad(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandDebug());
        event.registerServerCommand(new CommandSetHaunt());
    }

    @Mod.EventHandler
    public void serverStopped(FMLServerStoppedEvent event) {
        LostSoulData.clearInstance();
    }
}
