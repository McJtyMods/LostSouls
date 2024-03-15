package mcjty.lostsouls.setup;

import com.google.common.collect.Lists;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Config {

    public static final String CATEGORY_GENERAL = "general";

    public static ModConfigSpec.ConfigValue<List<? extends String>> COMMAND_FIRSTTIME;
    public static ModConfigSpec.ConfigValue<List<? extends String>> COMMAND_ENTERED;
    public static ModConfigSpec.ConfigValue<List<? extends String>> COMMAND_CLEARED;

    private static String[] DEF_COMMAND_FIRSTTIME = new String[]{};
    private static String[] DEF_COMMAND_ENTERED = new String[]{};
    private static String[] DEF_COMMAND_CLEARED = new String[]{};

    public static ForgeConfigSpec.ConfigValue<String> MESSAGE_UNSAFE_BUILDING;
    public static ForgeConfigSpec.ConfigValue<String> MESSAGE_BUILDING_HAUNTED;
    public static ForgeConfigSpec.ConfigValue<String> MESSAGE_BUILDING_CLEARED;
    public static ForgeConfigSpec.ConfigValue<String> MESSAGE_BUILDING_HALFWAY;
    private final static String DEF_MESSAGE_UNSAFE_BUILDING = "§eThe building isn't safe enough!";
    private final static String DEF_MESSAGE_BUILDING_HAUNTED = "§eThis building is haunted. Be careful!";
    private final static String DEF_MESSAGE_BUILDING_CLEARED = "§aThe building feels a lot safer now!";
    private final static String DEF_MESSAGE_BUILDING_HALFWAY = "§eAbout half way there! Keep going!";

    public static ModConfigSpec.IntValue SERVERTICK_TIMEOUT;// = 200;
    public static ModConfigSpec.IntValue SPAWN_MAX_NEARBY;// = 6;
    public static ModConfigSpec.DoubleValue MIN_SPAWN_DISTANCE;// = 8.0f;
    public static ModConfigSpec.DoubleValue HAUNTED_CHANCE;// = 0.8f;
    public static ModConfigSpec.IntValue MIN_MOBS;// = 10;
    public static ModConfigSpec.IntValue MAX_MOBS;// = 50;
    public static ModConfigSpec.DoubleValue SPHERE_HAUNTED_CHANCE;// = 0.8f;
    public static ModConfigSpec.IntValue SPHERE_MIN_MOBS;// = 10;
    public static ModConfigSpec.IntValue SPHERE_MAX_MOBS;// = 50;
    public static ModConfigSpec.DoubleValue MIN_HEALTH_BONUS;// = 2f;
    public static ModConfigSpec.DoubleValue MAX_HEALTH_BONUS;// = 5f;
    public static ModConfigSpec.DoubleValue MIN_DAMAGE_BONUS;// = 2f;
    public static ModConfigSpec.DoubleValue MAX_DAMAGE_BONUS;// = 5f;
    public static ModConfigSpec.BooleanValue CHECK_VALID_SPAWN;// = false;
    public static ModConfigSpec.BooleanValue SPAWN_ON_BLOCK;// = false;
    public static ModConfigSpec.BooleanValue LOCK_CHESTS_UNTIL_CLEARED;// = true;
    public static ModConfigSpec.BooleanValue LOCK_ONLY_CHESTS;// = true;
    public static ModConfigSpec.BooleanValue ANNOUNCE_CLEARED;// = true;
    public static ModConfigSpec.BooleanValue ANNOUNCE_ENTER;// = true;
    public static ModConfigSpec.BooleanValue ANNOUNCE_CHESTLOCKED;// = true;

    private static String[] DEF_EXCLUDED_BUILDINGS = new String[]{};
    private static String[] DEF_MOBS = new String[]{".3=minecraft:zombie", ".3=minecraft:spider", ".3=minecraft:skeleton", ".2=minecraft:husk", ".2=minecraft:stray", ".1=minecraft:witch", ".1=minecraft:enderman"};
    private static String[] DEF_RANDOM_WEAPONS = new String[]{".3=null", ".3=minecraft:diamond_sword", ".3=minecraft:iron_sword", ".3=minecraft:bow"};
    private static String[] DEF_RANDOM_HELMETS = new String[]{".3=null", ".3=minecraft:diamond_helmet", ".3=minecraft:iron_helmet"};
    private static String[] DEF_RANDOM_CHESTS = new String[]{".3=null", ".3=minecraft:diamond_chestplate", ".3=minecraft:iron_chestplate"};
    private static String[] DEF_RANDOM_LEGGINGS = new String[]{".3=null", ".3=minecraft:diamond_leggings", ".3=minecraft:iron_leggings"};
    private static String[] DEF_RANDOM_BOOTS = new String[]{".3=null", ".3=minecraft:diamond_boots", ".3=minecraft:iron_boots"};
    private static String[] DEF_RANDOM_EFFECTS = new String[]{".3=minecraft:regeneration,3", ".3=minecraft:speed,3", ".3=minecraft:fire_resistance,3"};

    public static ModConfigSpec.ConfigValue<List<? extends String>> EXCLUDED_BUILDINGS;
    public static ModConfigSpec.ConfigValue<List<? extends String>> MOBS;
    public static ModConfigSpec.ConfigValue<List<? extends String>> RANDOM_WEAPONS;
    public static ModConfigSpec.ConfigValue<List<? extends String>> RANDOM_HELMETS;
    public static ModConfigSpec.ConfigValue<List<? extends String>> RANDOM_CHESTS;
    public static ModConfigSpec.ConfigValue<List<? extends String>> RANDOM_LEGGINGS;
    public static ModConfigSpec.ConfigValue<List<? extends String>> RANDOM_BOOTS;
    public static ModConfigSpec.ConfigValue<List<? extends String>> RANDOM_EFFECTS;

    private static Set<String> excludedBuildings = null;
    private static List<Pair<Float, String>> randomMobs = null;
    private static List<Pair<Float, String>> randomWeapons = null;
    private static List<Pair<Float, String>> randomHelmets = null;
    private static List<Pair<Float, String>> randomChests = null;
    private static List<Pair<Float, String>> randomLeggings = null;
    private static List<Pair<Float, String>> randomBoots = null;
    private static List<Pair<Float, String>> randomEffects = null;

    private static final ModConfigSpec.Builder SERVER_BUILDER = new ModConfigSpec.Builder();

    public static void register() {
        SERVER_BUILDER.comment("General settings").push(CATEGORY_GENERAL);

        EXCLUDED_BUILDINGS = SERVER_BUILDER
                .comment("A list of buildings that are safe(r)")
                .defineList("excludedBuildings", Lists.newArrayList(Config.DEF_EXCLUDED_BUILDINGS), s -> s instanceof String);
        MOBS = SERVER_BUILDER
                .comment("List of mobs that can spawn in buildings together with their rarity")
                .defineList("mobs", Lists.newArrayList(DEF_MOBS), s -> s instanceof String);
        RANDOM_WEAPONS = SERVER_BUILDER
                .comment("List of weapons that the mobs can have together with their rarity")
                .defineList("randomWeapons", Lists.newArrayList(DEF_RANDOM_WEAPONS), s -> s instanceof String);
        RANDOM_HELMETS = SERVER_BUILDER
                .comment("List of helmets that the mobs can have together with their rarity")
                .defineList("randomHelmets", Lists.newArrayList(DEF_RANDOM_HELMETS), s -> s instanceof String);
        RANDOM_CHESTS = SERVER_BUILDER
                .comment("List of chestplates that the mobs can have together with their rarity")
                .defineList("randomChestplates", Lists.newArrayList(DEF_RANDOM_CHESTS), s -> s instanceof String);
        RANDOM_LEGGINGS = SERVER_BUILDER
                .comment("List of leggings that the mobs can have together with their rarity")
                .defineList("randomLeggings", Lists.newArrayList(DEF_RANDOM_LEGGINGS), s -> s instanceof String);
        RANDOM_BOOTS = SERVER_BUILDER
                .comment("List of boots that the mobs can have together with their rarity")
                .defineList("randomBoots", Lists.newArrayList(DEF_RANDOM_BOOTS), s -> s instanceof String);
        RANDOM_EFFECTS = SERVER_BUILDER
                .comment("List of effects that a mob can have. Note that multiple effects are possible")
                .defineList("randomEffects", Lists.newArrayList(DEF_RANDOM_EFFECTS), s -> s instanceof String);

        COMMAND_FIRSTTIME = SERVER_BUILDER
                .comment("List of console commands to execute the first time a building is entered")
                .defineList("commandFirstTime", Lists.newArrayList(DEF_COMMAND_FIRSTTIME), s -> s instanceof String);
        COMMAND_ENTERED = SERVER_BUILDER
                .comment("List of console commands to execute every time a building is entered")
                .defineList("commandEntered", Lists.newArrayList(DEF_COMMAND_ENTERED), s -> s instanceof String);
        COMMAND_CLEARED = SERVER_BUILDER
                .comment("List of console commands to execute when a building is cleared")
                .defineList("commandCleared", Lists.newArrayList(DEF_COMMAND_CLEARED), s -> s instanceof String);

        MESSAGE_UNSAFE_BUILDING = SERVER_BUILDER
                .comment("This message is given when the player tries to open a chest in a haunted building")
                .define("messageUnsafeBuilding", DEF_MESSAGE_UNSAFE_BUILDING);
        MESSAGE_BUILDING_HAUNTED = SERVER_BUILDER
                .comment("This message is given when the player enters a haunted building for the first time")
                .define("messageBuildingHaunted", DEF_MESSAGE_BUILDING_HAUNTED);
        MESSAGE_BUILDING_CLEARED = SERVER_BUILDER
                .comment("This message is given when the player clears a building")
                .define("messageBuildingCleared", DEF_MESSAGE_BUILDING_CLEARED);
        MESSAGE_BUILDING_HALFWAY = SERVER_BUILDER
                .comment("This message is given when the player is halfway clearing a building")
                .define("messageBuildingHalfway", DEF_MESSAGE_BUILDING_HALFWAY);


        SERVERTICK_TIMEOUT = SERVER_BUILDER
                .comment("The amount of ticks that the server waits before checking for new spawns")
                .defineInRange("serverTickTimeout", 200, 1, 1000000);
        SPAWN_MAX_NEARBY = SERVER_BUILDER
                .comment("The maximum amount of entities that can spawn near each other (of the same type)")
                .defineInRange("spawnMaxNearby", 6, 1, 200);
        MIN_SPAWN_DISTANCE = SERVER_BUILDER
                .comment("The minimum distance between the player and newly spawned mobs")
                .defineInRange("minSpawnDistance", 8.0f, 0, 16);
        MIN_HEALTH_BONUS = SERVER_BUILDER
                .comment("The minimum health bonus that the mob will get")
                .defineInRange("minHealthBonus", 2f, 0.01f, 10000);
        MAX_HEALTH_BONUS = SERVER_BUILDER
                .comment("The maximum health bonus that the mob will get")
                .defineInRange("maxHealthBonus", 5f, 0.01f, 10000);
        MIN_DAMAGE_BONUS = SERVER_BUILDER
                .comment("The minimum damage bonus that the mob will get")
                .defineInRange("minDamageBonus", 2f, 0.01f, 10000);
        MAX_DAMAGE_BONUS = SERVER_BUILDER
                .comment("The maximum damage bonus that the mob will get")
                .defineInRange("maxDamageBonus", 5f, 0.01f, 10000);

        HAUNTED_CHANCE = SERVER_BUILDER
                .comment("The chance that a building is haunted")
                .defineInRange("hauntedChance", 0.8f, 0, 1);
        MIN_MOBS = SERVER_BUILDER
                .comment("The minimum amount of mobs that are spawned by a haunted building")
                .defineInRange("minMobs", 10, 1, 10000);
        MAX_MOBS = SERVER_BUILDER
                .comment("The maximum amount of mobs that are spawned by a haunted building")
                .defineInRange("maxMobs", 50, 1, 10000);
        SPHERE_HAUNTED_CHANCE = SERVER_BUILDER
                .comment("The chance that a building is haunted. This version is used in case the building is in a Lost City sphere")
                .defineInRange("sphereHauntedChance", 0.8f, 0, 1);
        SPHERE_MIN_MOBS = SERVER_BUILDER
                .comment("The minimum amount of mobs that are spawned by a haunted building. This version is used in case the building is in a Lost City sphere")
                .defineInRange("sphereMinMobs", 10, 1, 10000);
        SPHERE_MAX_MOBS = SERVER_BUILDER
                .comment("The maximum amount of mobs that are spawned by a haunted building. This version is used in case the building is in a Lost City sphere")
                .defineInRange("sphereMaxMobs", 50, 1, 10000);

        CHECK_VALID_SPAWN = SERVER_BUILDER
                .comment("If this is true then mobs will only spawn if the light level is low enough. Otherwise they spawn regardless of light level")
                .define("checkValidSpawn", false);
        SPAWN_ON_BLOCK = SERVER_BUILDER
                .comment("If this is true then mobs will only spawn on blocks but not on air")
                .define("spawnOnBlock", false);
        ANNOUNCE_CLEARED = SERVER_BUILDER
                .comment("If this is true then the player will be notified when a building is cleared")
                .define("announceCleared", true);
        ANNOUNCE_ENTER = SERVER_BUILDER
                .comment("If this is true then the player will be notified when he or she enters a haunted building")
                .define("announceEnter", true);
        ANNOUNCE_CHESTLOCKED = SERVER_BUILDER
                .comment("If this is true then the player will get a message when he/she tries to open a locked chest")
                .define("announceChestLocked", true);
        LOCK_CHESTS_UNTIL_CLEARED = SERVER_BUILDER
                .comment("If this is true then all chests will be locked until the building is cleared")
                .define("lockChestsUntilCleared", true);
        LOCK_ONLY_CHESTS = SERVER_BUILDER
                .comment("This option is only useful when 'lockChestsUntilCleared'. If true only vanilla chests will be locked. Otherwise all tile entities are locked")
                .define("lockOnlyChests", true);

        SERVER_BUILDER.pop();
        SERVER_CONFIG = SERVER_BUILDER.build();
    }

    public static ModConfigSpec SERVER_CONFIG;

    public static Set<String> getExcludedBuildings() {
        if (excludedBuildings == null) {
            excludedBuildings = new HashSet<>();
            excludedBuildings.addAll(EXCLUDED_BUILDINGS.get());
        }
        return excludedBuildings;
    }

    public static List<Pair<Float, String>> getRandomMobs() {
        if (randomMobs == null) {
            randomMobs = new ArrayList<>();
            makeList(randomMobs, MOBS.get());
        }
        return randomMobs;
    }

    public static List<Pair<Float, String>> getRandomWeapons() {
        if (randomWeapons == null) {
            randomWeapons = new ArrayList<>();
            makeList(randomWeapons, RANDOM_WEAPONS.get());
        }
        return randomWeapons;
    }

    public static List<Pair<Float, String>> getRandomHelmets() {
        if (randomHelmets == null) {
            randomHelmets = new ArrayList<>();
            makeList(randomHelmets, RANDOM_HELMETS.get());
        }
        return randomHelmets;
    }

    public static List<Pair<Float, String>> getRandomChests() {
        if (randomChests == null) {
            randomChests = new ArrayList<>();
            makeList(randomChests, RANDOM_CHESTS.get());
        }
        return randomChests;
    }

    public static List<Pair<Float, String>> getRandomLeggings() {
        if (randomLeggings == null) {
            randomLeggings = new ArrayList<>();
            makeList(randomLeggings, RANDOM_LEGGINGS.get());
        }
        return randomLeggings;
    }

    public static List<Pair<Float, String>> getRandomBoots() {
        if (randomBoots == null) {
            randomBoots = new ArrayList<>();
            makeList(randomBoots, RANDOM_BOOTS.get());
        }
        return randomBoots;
    }

    public static List<Pair<Float, String>> getRandomEffects() {
        if (randomEffects == null) {
            randomEffects = new ArrayList<>();
            makeList(randomEffects, RANDOM_EFFECTS.get());
        }
        return randomEffects;
    }

    private static void makeList(List<Pair<Float, String>> list, List<? extends String> elements) {
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
}
