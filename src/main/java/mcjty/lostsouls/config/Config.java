package mcjty.lostsouls.config;

import net.minecraftforge.common.config.Configuration;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

public class Config {

    public static final String CATEGORY_GENERAL = "general";

    public static int SERVERTICK_TIMEOUT = 200;
    public static int SPAWN_MAX_NEARBY = 6;
    public static float MIN_SPAWN_DISTANCE = 8.0f;

    public static float HAUNTED_CHANCE = 0.8f;
    public static int MIN_MOBS = 10;
    public static int MAX_MOBS = 50;
    public static float SPHERE_HAUNTED_CHANCE = 0.8f;
    public static int SPHERE_MIN_MOBS = 10;
    public static int SPHERE_MAX_MOBS = 50;

    public static float MIN_HEALTH_BONUS = 2f;
    public static float MAX_HEALTH_BONUS = 5f;
    public static float MIN_DAMAGE_BONUS = 2f;
    public static float MAX_DAMAGE_BONUS = 5f;
    public static boolean CHECK_VALID_SPAWN = false;

    public static boolean LOCK_CHESTS_UNTIL_CLEARED = true;
    public static boolean LOCK_ONLY_CHESTS = true;

    public static boolean ANNOUNCE_CLEARED = true;
    public static boolean ANNOUNCE_ENTER = true;
    public static boolean ANNOUNCE_CHESTLOCKED = true;

    private static String[] EXCLUDED_BUILDINGS = new String[]{};
    private static String[] MOBS = new String[] { ".3=minecraft:zombie", ".3=minecraft:spider", ".3=minecraft:skeleton", ".2=minecraft:husk", ".2=minecraft:stray", ".1=minecraft:witch", ".1=minecraft:enderman" };
    private static String[] RANDOM_WEAPONS = new String[] { ".3=null", ".3=minecraft:diamond_sword", ".3=minecraft:iron_sword", ".3=minecraft:bow" };
    private static String[] RANDOM_HELMETS = new String[] { ".3=null", ".3=minecraft:diamond_helmet", ".3=minecraft:iron_helmet" };
    private static String[] RANDOM_CHESTS = new String[] { ".3=null", ".3=minecraft:diamond_chestplate", ".3=minecraft:iron_chestplate" };
    private static String[] RANDOM_LEGGINGS = new String[] { ".3=null", ".3=minecraft:diamond_leggings", ".3=minecraft:iron_leggings" };
    private static String[] RANDOM_BOOTS = new String[] { ".3=null", ".3=minecraft:diamond_boots", ".3=minecraft:iron_boots" };
    private static String[] RANDOM_EFFECTS = new String[] { ".3=minecraft:regeneration,3", ".3=minecraft:speed,3", ".3=minecraft:fire_resistance,3" };

    private static Set<String> excludedBuildings = null;
    private static List<Pair<Float, String>> randomMobs = null;
    private static List<Pair<Float, String>> randomWeapons = null;
    private static List<Pair<Float, String>> randomHelmets = null;
    private static List<Pair<Float, String>> randomChests = null;
    private static List<Pair<Float, String>> randomLeggings = null;
    private static List<Pair<Float, String>> randomBoots = null;
    private static List<Pair<Float, String>> randomEffects = null;

    public static String[] COMMAND_FIRSTTIME = new String[] {};
    public static String[] COMMAND_ENTERED = new String[] {};
    public static String[] COMMAND_CLEARED = new String[] {};

    public static String MESSAGE_UNSAFE_BUILDING = "The building isn't safe enough!";
    public static String MESSAGE_BUILDING_HAUNTED = "This building is haunted. Be careful!";
    public static String MESSAGE_BUILDING_CLEARED = "The building feels a lot safer now!";
    public static String MESSAGE_BUILDING_HALFWAY = "About half way there! Keep going!";

    public static Set<String> getExcludedBuildings() {
        if (excludedBuildings == null) {
            excludedBuildings = new HashSet<>();
            Collections.addAll(excludedBuildings, EXCLUDED_BUILDINGS);
        }
        return excludedBuildings;
    }

    public static List<Pair<Float, String>> getRandomMobs() {
        if (randomMobs == null) {
            randomMobs = new ArrayList<>();
            makeList(randomMobs, MOBS);
        }
        return randomMobs;
    }

    public static List<Pair<Float, String>> getRandomWeapons() {
        if (randomWeapons == null) {
            randomWeapons = new ArrayList<>();
            makeList(randomWeapons, RANDOM_WEAPONS);
        }
        return randomWeapons;
    }

    public static List<Pair<Float, String>> getRandomHelmets() {
        if (randomHelmets == null) {
            randomHelmets = new ArrayList<>();
            makeList(randomHelmets, RANDOM_HELMETS);
        }
        return randomHelmets;
    }

    public static List<Pair<Float, String>> getRandomChests() {
        if (randomChests == null) {
            randomChests = new ArrayList<>();
            makeList(randomChests, RANDOM_CHESTS);
        }
        return randomChests;
    }

    public static List<Pair<Float, String>> getRandomLeggings() {
        if (randomLeggings == null) {
            randomLeggings = new ArrayList<>();
            makeList(randomLeggings, RANDOM_LEGGINGS);
        }
        return randomLeggings;
    }

    public static List<Pair<Float, String>> getRandomBoots() {
        if (randomBoots == null) {
            randomBoots = new ArrayList<>();
            makeList(randomBoots, RANDOM_BOOTS);
        }
        return randomBoots;
    }

    public static List<Pair<Float, String>> getRandomEffects() {
        if (randomEffects == null) {
            randomEffects = new ArrayList<>();
            makeList(randomEffects, RANDOM_EFFECTS);
        }
        return randomEffects;
    }


    private static void makeList(List<Pair<Float, String>> list, String[] elements) {
        for (String s : elements) {
            String[] split = StringUtils.split(s, '=');
            try {
                float factor = Float.parseFloat(split[0]);
                list.add(Pair.of(factor, split[1]));
            } catch (NumberFormatException e) {
                throw new RuntimeException("Bad random factor in 'mobs' setting for Lost Souls configuration!");
            }
        }
    }

    public static void init(Configuration cfg) {
        cfg.addCustomCategoryComment(CATEGORY_GENERAL, "General settings");

        SERVERTICK_TIMEOUT = cfg.getInt("serverTickTimeout", CATEGORY_GENERAL, SERVERTICK_TIMEOUT, 1, 1000000, "The amount of ticks that the server waits before checking for new spawns");
        SPAWN_MAX_NEARBY = cfg.getInt("spawnMaxNearby", CATEGORY_GENERAL, SPAWN_MAX_NEARBY, 1, 200, "The maximum amount of entities that can spawn near each other (of the same type)");
        MIN_SPAWN_DISTANCE = cfg.getFloat("minSpawnDistance", CATEGORY_GENERAL, MIN_SPAWN_DISTANCE, 0, 16, "The minimum distance between the player and newly spawned mobs");
        MIN_HEALTH_BONUS = cfg.getFloat("minHealthBonus", CATEGORY_GENERAL, MIN_HEALTH_BONUS, 0.01f, 10000, "The minimum health bonus that the mob will get");
        MAX_HEALTH_BONUS = cfg.getFloat("maxHealthBonus", CATEGORY_GENERAL, MAX_HEALTH_BONUS, 0.01f, 10000, "The maximum health bonus that the mob will get");
        MIN_DAMAGE_BONUS = cfg.getFloat("minDamageBonus", CATEGORY_GENERAL, MIN_DAMAGE_BONUS, 0.01f, 10000, "The minimum damage bonus that the mob will get");
        MAX_DAMAGE_BONUS = cfg.getFloat("maxDamageBonus", CATEGORY_GENERAL, MAX_DAMAGE_BONUS, 0.01f, 10000, "The maximum damage bonus that the mob will get");

        HAUNTED_CHANCE = cfg.getFloat("hauntedChance", CATEGORY_GENERAL, HAUNTED_CHANCE, 0, 1, "The chance that a building is haunted");
        MIN_MOBS = cfg.getInt("minMobs", CATEGORY_GENERAL, MIN_MOBS, 1, 10000, "The minimum amount of mobs that are spawned by a haunted building");
        MAX_MOBS = cfg.getInt("maxMobs", CATEGORY_GENERAL, MAX_MOBS, 1, 10000, "The maximum amount of mobs that are spawned by a haunted building");
        SPHERE_HAUNTED_CHANCE = cfg.getFloat("sphereHauntedChance", CATEGORY_GENERAL, SPHERE_HAUNTED_CHANCE, 0, 1, "The chance that a building is haunted. This version is used in case the building is in a Lost City sphere");
        SPHERE_MIN_MOBS = cfg.getInt("sphereMinMobs", CATEGORY_GENERAL, SPHERE_MIN_MOBS, 1, 10000, "The minimum amount of mobs that are spawned by a haunted building. This version is used in case the building is in a Lost City sphere");
        SPHERE_MAX_MOBS = cfg.getInt("sphereMaxMobs", CATEGORY_GENERAL, SPHERE_MAX_MOBS, 1, 10000, "The maximum amount of mobs that are spawned by a haunted building. This version is used in case the building is in a Lost City sphere");

        EXCLUDED_BUILDINGS = cfg.getStringList("excludedBuildings", CATEGORY_GENERAL, EXCLUDED_BUILDINGS, "List of building names where this mod will not spawn mobs");
        CHECK_VALID_SPAWN = cfg.getBoolean("checkValidSpawn", CATEGORY_GENERAL, CHECK_VALID_SPAWN, "If this is true then mobs will only spawn if the light level is low enough. Otherwise they spawn regardless of light level");
        ANNOUNCE_CLEARED = cfg.getBoolean("announceCleared", CATEGORY_GENERAL, ANNOUNCE_CLEARED, "If this is true then the player will be notified when a building is cleared");
        ANNOUNCE_ENTER = cfg.getBoolean("announceEnter", CATEGORY_GENERAL, ANNOUNCE_ENTER, "If this is true then the player will be notified when he or she enters a haunted building");
        ANNOUNCE_CHESTLOCKED = cfg.getBoolean("announceChestLocked", CATEGORY_GENERAL, ANNOUNCE_CHESTLOCKED, "If this is true then the player will get a message when he/she tries to open a locked chest");
        LOCK_CHESTS_UNTIL_CLEARED = cfg.getBoolean("lockChestsUntilCleared", CATEGORY_GENERAL, LOCK_CHESTS_UNTIL_CLEARED, "If this is true then all chests will be locked until the building is cleared");
        LOCK_ONLY_CHESTS = cfg.getBoolean("lockOnlyChests", CATEGORY_GENERAL, LOCK_ONLY_CHESTS, "This option is only useful when 'lockChestsUntilCleared'. If true only vanilla chests will be locked. Otherwise all tile entities are locked");
        MOBS = cfg.getStringList("mobs", CATEGORY_GENERAL, MOBS, "List of mobs that can spawn in buildings together with their rarity");
        RANDOM_WEAPONS = cfg.getStringList("randomWeapons", CATEGORY_GENERAL, RANDOM_WEAPONS, "List of weapons that the mobs can have together with their rarity");
        RANDOM_HELMETS = cfg.getStringList("randomHelmets", CATEGORY_GENERAL, RANDOM_HELMETS, "List of helmets that the mobs can have together with their rarity");
        RANDOM_CHESTS = cfg.getStringList("randomChestplates", CATEGORY_GENERAL, RANDOM_CHESTS, "List of chestplates that the mobs can have together with their rarity");
        RANDOM_LEGGINGS = cfg.getStringList("randomLeggings", CATEGORY_GENERAL, RANDOM_LEGGINGS, "List of leggings that the mobs can have together with their rarity");
        RANDOM_BOOTS = cfg.getStringList("randomBoots", CATEGORY_GENERAL, RANDOM_BOOTS, "List of boots that the mobs can have together with their rarity");
        RANDOM_EFFECTS = cfg.getStringList("randomEffects", CATEGORY_GENERAL, RANDOM_EFFECTS, "List of effects that a mob can have. Note that multiple effects are possible");

        COMMAND_FIRSTTIME = cfg.getStringList("commandFirstTime", CATEGORY_GENERAL, COMMAND_FIRSTTIME, "List of console commands to execute the first time a building is entered");
        COMMAND_ENTERED = cfg.getStringList("commandEntered", CATEGORY_GENERAL, COMMAND_ENTERED, "List of console commands to execute every time a building is entered");
        COMMAND_CLEARED = cfg.getStringList("commandCleared", CATEGORY_GENERAL, COMMAND_CLEARED, "List of console commands to execute when a building is cleared");

        MESSAGE_UNSAFE_BUILDING = cfg.getString("messageUnsafeBuilding", CATEGORY_GENERAL, MESSAGE_UNSAFE_BUILDING, "This message is given when the player tries to open a chest in a haunted building");
        MESSAGE_BUILDING_HAUNTED = cfg.getString("messageBuildingHaunted", CATEGORY_GENERAL, MESSAGE_BUILDING_HAUNTED, "This message is given when the player enters a haunted building for the first time");
        MESSAGE_BUILDING_CLEARED = cfg.getString("messageBuildingCleared", CATEGORY_GENERAL, MESSAGE_BUILDING_CLEARED, "This message is given when the player clears a building");
        MESSAGE_BUILDING_HALFWAY = cfg.getString("messageBuildingHalfway", CATEGORY_GENERAL, MESSAGE_BUILDING_HALFWAY, "This message is given when the player is halfway clearing a building");
    }
}
