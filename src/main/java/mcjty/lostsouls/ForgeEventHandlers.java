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
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ForgeEventHandlers {

    public static int timeout = -1;

    // This map keeps the last known chunk position for every player
    private Map<UUID, ChunkCoord> playerChunks = new HashMap<>();

    @SubscribeEvent
    public void commandRegister(RegisterCommandsEvent event) {
        ModCommands.register(event.getDispatcher());
    }

    @SubscribeEvent
    public void onPlayerInteract(PlayerInteractEvent event) {
        Level world = event.getLevel();
        if (Config.LOCK_CHESTS_UNTIL_CLEARED.get() && !world.isClientSide) {
            ILostCityInformation info = ModSetup.lostCities.getLostInfo(world);
            if (info != null) {
                BlockPos pos = event.getPos();
                BlockEntity te = world.getBlockEntity(pos);
                if ((Config.LOCK_ONLY_CHESTS.get() && te instanceof ChestBlockEntity) || ((!Config.LOCK_ONLY_CHESTS.get() && te != null))) {
                    int chunkX = pos.getX() >> 4;
                    int chunkZ = pos.getZ() >> 4;
                    LostChunkData data = LostSoulData.getSoulData(world, chunkX, chunkZ, info);
                    ILostChunkInfo chunkInfo = info.getChunkInfo(chunkX, chunkZ);
                    String buildingType = chunkInfo.getBuildingType();
                    if (isHaunted(data, buildingType)) {
                        event.setCanceled(true);
                        if (Config.ANNOUNCE_CHESTLOCKED.get()) {
                            MutableComponent unsafeMessage = Component.translatable(Config.MESSAGE_BUILDING_HAUNTED.get());
                            event.getEntity().sendSystemMessage(unsafeMessage);
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onTickEvent(TickEvent.ServerTickEvent event) {
        timeout--;
        if (timeout > 0) {
            return;
        }
        timeout = Config.SERVERTICK_TIMEOUT.get();

        PlayerList list = ServerLifecycleHooks.getCurrentServer().getPlayerList();
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
                MobSettings settings = data.getSettings();
                if (settings == null) {
                    LostSoulData ld = LostSoulData.getData(world);
                    settings = ld.getSettingsForChunk(world, new ChunkCoord(world.dimension(), chunkX, chunkZ), lost);
                    data.setSettings(settings);
                }
                if (entered) {
                    data.enterBuilding();
                    LostSoulData.getData(world).setDirty();
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


                int realHeight = lost.getRealHeight(chunkInfo.getCityLevel());

                // Restrict spawning to roughly the dimensions of the building
                int miny = realHeight - (chunkInfo.getNumCellars() + 1) * 6;
                int maxy = realHeight + (chunkInfo.getNumFloors() + 1) * 6;

                if (position.getY() >= miny && position.getY() <= maxy) {

                    double x = chunkX * 16 + rand.nextDouble() * 16.0;
                    double y = (position.getY() + rand.nextInt(3) - 1);
                    double z = chunkZ * 16 + rand.nextDouble() * 16.0;

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
                        if (distance >= Config.MIN_SPAWN_DISTANCE.get()) {
                            ResourceLocation mob = Tools.getRandomFromList(rand, settings.getMobs());
                            EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(mob);
                            if (type == null) {
                                throw new RuntimeException("Unknown entity '" + mob + "'!");
                            }
                            Entity entity = type.create(world);
                            int cnt = world.getEntities(entity, (new AABB(x, y, z, x + 1, y + 1, z + 1).inflate(8.0))).size();
                            if (cnt <= Config.SPAWN_MAX_NEARBY.get()) {
                                entity.setPos(x, y, z);
                                entity.setXRot(rand.nextFloat() * 360.0F);
                                if (entity instanceof Mob mobEntity) {
                                    if (!Config.CHECK_VALID_SPAWN.get() || (mobEntity.checkSpawnObstruction(world))) {
                                        boostEntity(settings, world, (LivingEntity) entity);

                                        entity.addTag("_ls_/" + world.dimension().location().toString() + "/" + chunkX + "/" + chunkZ);
                                        world.addFreshEntity(entity);
                                    }
                                }
                            }
                        }
                    }
                }
            }
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
                MobEffect value = ForgeRegistries.MOB_EFFECTS.getValue(effect);
                if (value == null) {
                    throw new RuntimeException("Cannot find potion effect '" + effect + "'!");
                }
                int amplitude = pair.level();
                entity.addEffect(new MobEffectInstance(value, 10000, amplitude));
            }
        }

        ResourceLocation weapon = Tools.getRandomFromList(rand, settings.getWeapons());
        if (weapon != null) {
            Item item = ForgeRegistries.ITEMS.getValue(weapon);
            if (item != null) {
                entity.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(item));
            }
        }
        ResourceLocation helmet = Tools.getRandomFromList(rand, settings.getHelmets());
        if (helmet != null) {
            Item item = ForgeRegistries.ITEMS.getValue(helmet);
            if (item != null) {
                entity.setItemSlot(EquipmentSlot.HEAD, new ItemStack(item));
            }
        }
        ResourceLocation chestplate = Tools.getRandomFromList(rand, settings.getChestplates());
        if (chestplate != null) {
            Item item = ForgeRegistries.ITEMS.getValue(chestplate);
            if (item != null) {
                entity.setItemSlot(EquipmentSlot.CHEST, new ItemStack(item));
            }
        }
        ResourceLocation leggings = Tools.getRandomFromList(rand, settings.getLeggings());
        if (leggings != null) {
            Item item = ForgeRegistries.ITEMS.getValue(leggings);
            if (item != null) {
                entity.setItemSlot(EquipmentSlot.LEGS, new ItemStack(item));
            }
        }
        ResourceLocation boots = Tools.getRandomFromList(rand, settings.getBoots());
        if (boots != null) {
            Item item = ForgeRegistries.ITEMS.getValue(boots);
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
                        // Should be in the cache, so we don't need a provider
                        LostChunkData data = LostSoulData.getSoulData(event.getEntity().level(), x, z, null);
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
