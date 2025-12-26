package mcjty.lostsouls;

import mcjty.lostcities.api.ILostChunkInfo;
import mcjty.lostcities.api.ILostCityInformation;
import mcjty.lostsouls.commands.ModCommands;
import mcjty.lostsouls.data.LostChunkData;
import mcjty.lostsouls.data.LostSoulData;
import mcjty.lostsouls.data.MobSettings;
import mcjty.lostsouls.setup.Config;
import mcjty.lostsouls.setup.ModSetup;
import mcjty.lostsouls.varia.ChunkCoord;
import mcjty.lostsouls.varia.Tools;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class ForgeEventHandlers {

    public static int timeout = -1;

    // This map keeps the last known chunk position for every player
    private Map<UUID, ChunkCoord> playerChunks = new HashMap<>();

    @SubscribeEvent
    public void commandRegister(RegisterCommandsEvent event) {
        ModCommands.register(event.getDispatcher());
    }

    @SubscribeEvent
    public void onPlayerInteract(PlayerInteractEvent.RightClickBlock event) {
        Level world = event.getLevel();
        if (Config.LOCK_CHESTS_UNTIL_CLEARED.get() && !world.isClientSide) {
            ILostCityInformation info = ModSetup.lostCities.getLostInfo(world);
            if (info != null) {
                BlockPos pos = event.getPos();
                BlockEntity te = world.getBlockEntity(pos);
                if ((Config.LOCK_ONLY_CHESTS.get() && te instanceof RandomizableContainerBlockEntity) || ((!Config.LOCK_ONLY_CHESTS.get() && te != null))) {
                    int chunkX = pos.getX() >> 4;
                    int chunkZ = pos.getZ() >> 4;
                    LostChunkData data = LostSoulData.getSoulData(world, chunkX, chunkZ, info);
                    ILostChunkInfo chunkInfo = info.getChunkInfo(chunkX, chunkZ);
                    String buildingType = chunkInfo.getBuildingType();
                    if (isHaunted(data, buildingType)) {
                        event.setCanceled(true);
                        if (Config.ANNOUNCE_CHESTLOCKED.get()) {
                            MutableComponent unsafeMessage = Component.translatable(Config.MESSAGE_UNSAFE_BUILDING.get());
                            event.getEntity().sendSystemMessage(unsafeMessage);
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onTickEvent(ServerTickEvent.Post event) {
        timeout--;
        if (timeout > 0) {
            return;
        }
        timeout = Config.SERVERTICK_TIMEOUT.get();

        PlayerList list = event.getServer().getPlayerList();
        for (ServerPlayer player : list.getPlayers()) {

            UUID uuid = player.getUUID();
            BlockPos position = player.blockPosition();
            int chunkX = position.getX() >> 4;
            int chunkZ = position.getZ() >> 4;
            ChunkCoord chunkCoord = new ChunkCoord(player.level().dimension(), chunkX, chunkZ);
            boolean entered = false;

            if (!playerChunks.containsKey(uuid)) {
                playerChunks.put(uuid, chunkCoord);
                entered = true;
            } else {
                ChunkCoord oldPos = playerChunks.get(uuid);
                if (!oldPos.equals(chunkCoord)) {
                    // Newly entered chunk
                    playerChunks.put(uuid, chunkCoord);
                    entered = true;
                }
            }


            ILostCityInformation info = ModSetup.lostCities.getLostInfo(player.level());
            if (info != null) {
                handleSpawn(player, info, entered);
            }
        }
    }

    private boolean isHaunted(LostChunkData data, String buildingType) {
        if (buildingType == null) {
            return false;
        }
        if (Config.getExcludedBuildings().contains(buildingType)) {
            return false;
        }
        return data.isHaunted() && data.getNumberKilled() < data.getTotalMobs();
    }

    private void handleSpawn(ServerPlayer player, ILostCityInformation lost, boolean entered) {
        BlockPos position = player.blockPosition();
        int chunkX = position.getX() >> 4;
        int chunkZ = position.getZ() >> 4;
        ILostChunkInfo chunkInfo = lost.getChunkInfo(chunkX, chunkZ);
        String buildingType = chunkInfo.getBuildingType();
        if (buildingType != null) {
            // We have a building
            ServerLevel world = (ServerLevel) player.level();
            RandomSource rand = world.getRandom();
            long gameTime = world.getGameTime();
            LostChunkData data = LostSoulData.getSoulData(world, chunkX, chunkZ, lost);

            if (isHaunted(data, buildingType)) {
                // Restrict spawning to roughly the dimensions of the building
                int realHeight = lost.getRealHeight(chunkInfo.getCityLevel());

                int miny = realHeight - (chunkInfo.getNumCellars() + 1) * 6;
                int maxy = realHeight + (chunkInfo.getNumFloors() + 1) * 6;

                if (!(position.getY() >= miny && position.getY() <= maxy)) return;

                LostSoulData ld = LostSoulData.getData(world);
                MobSettings settings = data.getSettings();
                if (settings == null) {
                    settings = ld.getSettingsForChunk(world, new ChunkCoord(world.dimension(), chunkX, chunkZ), lost);
                    data.setSettings(settings);
                }
                if (entered) {
                    data.enterBuilding();
                    ld.setDirty();
                    enterBuilding(player, data, gameTime, world);
                }
                // If it's a multibuilding, we can try to randomize between the multichunks instead of fixated on single chunk.
                ILostChunkInfo.MultiBuildingInfo mb = chunkInfo.getMultiBuildingInfo();
                int rootX = chunkX;
                int rootZ = chunkZ;
                int maxChunkX = chunkX;
                int maxChunkZ = chunkZ;
                if (mb != null) {
                    rootX = chunkX - mb.offsetX();
                    rootZ = chunkZ - mb.offsetZ();
                    chunkX = rootX + rand.nextInt(0, mb.w());
                    chunkZ = rootZ + rand.nextInt(0, mb.h());
                    maxChunkX = rootX + mb.w();
                    maxChunkZ = rootZ + mb.h();
                }

                double x = chunkX * 16 + Math.floor(rand.nextDouble() * 16.0) + 0.5;
                double y = (position.getY() + rand.nextInt(3) - 1);
                double z = chunkZ * 16 + Math.floor(rand.nextDouble() * 16.0) + 0.5;

                if (world.getBlockState(new BlockPos((int) x, (int) (y - 1), (int) z)).isAir()) {
                    y--;
                }
                if (!world.getBlockState(new BlockPos((int) x, (int) y, (int) z)).isAir()) {
                    y++;
                }
                boolean allowSpawn = true;
                if (Config.SPAWN_ON_BLOCK.get()) {
                    allowSpawn = !world.getBlockState(new BlockPos((int) x, (int) (y - 1), (int) z)).isAir();
                }
                if (allowSpawn && world.getBlockState(new BlockPos((int) x, (int) y, (int) z)).isAir()) {
                    double distance = Math.sqrt(position.distToCenterSqr((int) x, (int) y, (int) z));
                    if (distance >= Config.MIN_SPAWN_DISTANCE.get() && distance <= Config.MAX_SPAWN_DISTANCE.get()) {
                        int totalMobsToSpawn = data.getTotalMobs() - data.getNumberKilled();
                        spawnMob(rand, settings, world, x, y, z, rootX, rootZ, maxChunkX, maxChunkZ, miny, maxy, totalMobsToSpawn);
                    }
                }
            }
        }
    }

    private void spawnMob(RandomSource rand, MobSettings settings, ServerLevel world, double x, double y, double z, int chunkX, int chunkZ, int maxChunkX, int maxChunkZ, int minY, int maxY, int totalMobs) {
        ResourceLocation mob = Tools.getRandomFromList(rand, settings.getMobs());
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(mob);
        if (type == null) {
            throw new RuntimeException("Unknown entity '" + mob + "'!");
        }
        int cnt = 0;
        int maxEntities = 0;
        if (Config.USE_CHUNK_CHECK.get()) {
            maxEntities = totalMobs;
            List<Entity> entityList = world.getEntities(
                    (Entity) null,
                    new AABB(chunkX * 16, minY, chunkZ * 16, (maxChunkX+1) * 16, maxY, (maxChunkZ+1) * 16),
                    entity -> entity.getTags().stream().anyMatch(tag -> tag.contains("_ls_/"))
            );
            cnt = entityList.size();
        } else {
            maxEntities = Config.SPAWN_MAX_NEARBY.get();
            cnt = world.getEntities((Entity) null,
                    new AABB(x, y, z, x + 1, y + 1, z + 1).inflate(Config.SPAWN_MAX_NEARBY_RADIUS.get()),
                    entity -> entity.getTags().stream().anyMatch(tag -> tag.contains("_ls_/"))
            ).size();
        }
        if (cnt <= maxEntities) {
            Entity entity = type.create(world);
            entity.setPos(x, y, z);
            entity.setXRot(rand.nextFloat() * 360.0F);
            if (entity instanceof Mob mobEntity) {
                if (!Config.CHECK_VALID_SPAWN.get() || (mobEntity.checkSpawnObstruction(world))) {
                    boostEntity(settings, world, (LivingEntity) entity);

                    entity.addTag("_ls_/" + world.dimension().location() + "/" + chunkX + "/" + chunkZ);
                    mobEntity.finalizeSpawn(
                            world,
                            world.getCurrentDifficultyAt(mobEntity.blockPosition()),
                            MobSpawnType.MOB_SUMMONED, // or NATURAL, COMMAND, etc
                            null
                    );
                    world.addFreshEntity(entity);
                }
            }
        }
    }

    private void enterBuilding(ServerPlayer player, LostChunkData data, long gameTime, ServerLevel world) {
        int enteredCount = data.getEnteredCount();
        if (Config.ANNOUNCE_ENTER.get()) {
            long lastMessagetime = data.getMessagetime();
            if (enteredCount == 1) {
                // First time
                MutableComponent firstMessage = Component.translatable(Config.MESSAGE_BUILDING_HAUNTED.get());
                player.sendSystemMessage(firstMessage);
            } else if (lastMessagetime + Config.MESSAGE_INTERVAL.get() < gameTime) {
                String msg = Config.MESSAGE_BUILDING_HAUNTED_REPEAT.get();
                if ("<same>".equals(msg)) {
                    msg = Config.MESSAGE_BUILDING_HAUNTED.get();
                }
                MutableComponent message = Component.translatable(msg);
                player.sendSystemMessage(message);
            }
            data.setMessagetime(gameTime);
        }
        if (enteredCount == 1) {
            executeCommands(player, world, Config.COMMAND_FIRSTTIME.get());
        }
        if (enteredCount >= 1) {
            executeCommands(player, world, Config.COMMAND_ENTERED.get());
        }
    }

    private static final Component DEFAULT_NAME = Component.literal("@");
    private static final CommandSource EMPTY = new CommandSource() {
        @Override
        public void sendSystemMessage(Component component) {
        }

        @Override
        public boolean acceptsSuccess() {
            return false;
        }

        @Override
        public boolean acceptsFailure() {
            return false;
        }

        @Override
        public boolean shouldInformAdmins() {
            return false;
        }
    };

    private void executeCommands(ServerPlayer player, Level world, List<? extends String> commands) {
        if (!commands.isEmpty()) {
            BlockPos pos = player.blockPosition();
            ServerLevel level = (ServerLevel) world;
            CommandSourceStack stack = new CommandSourceStack(EMPTY, Vec3.atCenterOf(pos), Vec2.ZERO, level, 2,
                    DEFAULT_NAME.getString(), DEFAULT_NAME, level.getServer(), player);
            for (String command : commands) {
                level.getServer().getCommands().performPrefixedCommand(stack, command);
            }
        }
    }

    private void boostEntity(MobSettings settings, Level world, LivingEntity entity) {
        AttributeInstance entityAttribute = entity.getAttribute(Attributes.MAX_HEALTH);
        RandomSource rand = world.getRandom();
        if (entityAttribute != null) {
            double f = rand.nextFloat() * (Config.MAX_HEALTH_BONUS.get() - Config.MIN_HEALTH_BONUS.get()) + Config.MIN_HEALTH_BONUS.get();
            double newMax = entityAttribute.getBaseValue() * f;
            entityAttribute.setBaseValue(newMax);
            entity.setHealth((float) newMax);
        }
        entityAttribute = entity.getAttribute(Attributes.ATTACK_DAMAGE);
        if (entityAttribute != null) {
            double f = rand.nextFloat() * (Config.MAX_DAMAGE_BONUS.get() - Config.MIN_DAMAGE_BONUS.get()) + Config.MIN_DAMAGE_BONUS.get();
            double newMax = entityAttribute.getBaseValue() * f;
            entityAttribute.setBaseValue(newMax);
        }

        for (MobSettings.Effect pair : settings.getEffects()) {
            if (rand.nextFloat() < pair.weight()) {
                ResourceLocation effect = pair.name();
                Optional<Holder.Reference<MobEffect>> value = BuiltInRegistries.MOB_EFFECT.getHolder(effect);
                if (value.isEmpty()) {
                    throw new RuntimeException("Cannot find potion effect '" + effect + "'!");
                }
                int amplitude = pair.level();
                entity.addEffect(new MobEffectInstance(value.get(), 1000000, amplitude));
            }
        }

        ResourceLocation weapon = Tools.getRandomFromList(rand, settings.getWeapons());
        if (weapon != null) {
            Item item = BuiltInRegistries.ITEM.get(weapon);
            if (item != null) {
                entity.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(item));
            }
        }
        ResourceLocation helmet = Tools.getRandomFromList(rand, settings.getHelmets());
        if (helmet != null) {
            Item item = BuiltInRegistries.ITEM.get(helmet);
            if (item != null) {
                entity.setItemSlot(EquipmentSlot.HEAD, new ItemStack(item));
            }
        }
        ResourceLocation chestplate = Tools.getRandomFromList(rand, settings.getChestplates());
        if (chestplate != null) {
            Item item = BuiltInRegistries.ITEM.get(chestplate);
            if (item != null) {
                entity.setItemSlot(EquipmentSlot.CHEST, new ItemStack(item));
            }
        }
        ResourceLocation leggings = Tools.getRandomFromList(rand, settings.getLeggings());
        if (leggings != null) {
            Item item = BuiltInRegistries.ITEM.get(leggings);
            if (item != null) {
                entity.setItemSlot(EquipmentSlot.LEGS, new ItemStack(item));
            }
        }
        ResourceLocation boots = Tools.getRandomFromList(rand, settings.getBoots());
        if (boots != null) {
            Item item = BuiltInRegistries.ITEM.get(boots);
            if (item != null) {
                entity.setItemSlot(EquipmentSlot.FEET, new ItemStack(item));
            }
        }
    }

    @SubscribeEvent
    public void onKill(LivingDeathEvent event) {
        Entity source = event.getSource().getEntity();
        if (source instanceof ServerPlayer player) {
            for (String tag : event.getEntity().getTags()) {
                if (tag.startsWith("_ls_/")) {
                    // 'tag' is a string delimited with /. It can have any number of parts. We only need the two last parts as integer
                    String[] split = StringUtils.split(tag, '/');
                    try {
//                        String dim = split[1];
                        int x = Integer.parseInt(split[split.length - 2]);
                        int z = Integer.parseInt(split[split.length - 1]);

                        ILostCityInformation info = ModSetup.lostCities.getLostInfo(player.level());
                        LostChunkData data = LostSoulData.getSoulData(event.getEntity().level(), x, z, info);
                        MobSettings settings = data.getSettings();
                        if (settings != null) {
                            LostSoulData ld = LostSoulData.getData(event.getEntity().level());
                            settings = ld.getSettingsForChunk((ServerLevel)event.getEntity().level(), new ChunkCoord(event.getEntity().level().dimension(), x, z), info);
                            data.setSettings(settings);
                        }

                        data.newKill();
                        if (Config.ANNOUNCE_CLEARED.get()) {
                            if (data.getNumberKilled() == data.getTotalMobs()) {
                                MutableComponent clearMessage = Component.translatable(Config.MESSAGE_BUILDING_CLEARED.get());
                                player.sendSystemMessage(clearMessage);
                                executeCommands(player, source.getCommandSenderWorld(), Config.COMMAND_CLEARED.get());
                            } else if (data.getNumberKilled() == data.getTotalMobs() / 2) {
                                MutableComponent halfwayMessage = Component.translatable(Config.MESSAGE_BUILDING_HALFWAY.get());
                                player.sendSystemMessage(halfwayMessage);
                            }
                        }
                        LostSoulData.getData(event.getEntity().level()).setDirty();
                    } catch (NumberFormatException e) {
                        LostSouls.logger.error("ForgeEventHandlers.onKill ERROR", e);
                    }
                    return;
                }
            }
        }
    }

}
