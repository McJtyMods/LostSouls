package mcjty.lostsouls.config;

import mcjty.lostsouls.LostSouls;
import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.logging.log4j.Level;

import java.io.File;

public class ConfigSetup {

    public static void init() {
        mainConfig = new Configuration(new File(LostSouls.setup.modConfigDir.getPath(), "lostsouls.cfg"));
        Configuration cfg = mainConfig;
        try {
            cfg.load();
            cfg.addCustomCategoryComment(CATEGORY_GENERAL, "General settings");

            Config.COMMAND_FIRSTTIME = cfg.getStringList("commandFirstTime", CATEGORY_GENERAL, Config.COMMAND_FIRSTTIME, "List of console commands to execute the first time a building is entered");
            Config.COMMAND_ENTERED = cfg.getStringList("commandEntered", CATEGORY_GENERAL, Config.COMMAND_ENTERED, "List of console commands to execute every time a building is entered");
            Config.COMMAND_CLEARED = cfg.getStringList("commandCleared", CATEGORY_GENERAL, Config.COMMAND_CLEARED, "List of console commands to execute when a building is cleared");

            Config.MESSAGE_UNSAFE_BUILDING = cfg.getString("messageUnsafeBuilding", CATEGORY_GENERAL, Config.MESSAGE_UNSAFE_BUILDING, "This message is given when the player tries to open a chest in a haunted building");
            Config.MESSAGE_BUILDING_HAUNTED = cfg.getString("messageBuildingHaunted", CATEGORY_GENERAL, Config.MESSAGE_BUILDING_HAUNTED, "This message is given when the player enters a haunted building for the first time");
            Config.MESSAGE_BUILDING_CLEARED = cfg.getString("messageBuildingCleared", CATEGORY_GENERAL, Config.MESSAGE_BUILDING_CLEARED, "This message is given when the player clears a building");
            Config.MESSAGE_BUILDING_HALFWAY = cfg.getString("messageBuildingHalfway", CATEGORY_GENERAL, Config.MESSAGE_BUILDING_HALFWAY, "This message is given when the player is halfway clearing a building");
        } catch (Exception e1) {
            FMLLog.log(Level.ERROR, e1, "Problem loading config file!");
        }
    }

    public static void postInit() {
        if (mainConfig.hasChanged()) {
            mainConfig.save();
        }
    }
}
