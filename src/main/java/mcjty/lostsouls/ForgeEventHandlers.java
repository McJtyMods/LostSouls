package mcjty.lostsouls;

import mcjty.lostcities.api.ILostChunkInfo;
import mcjty.lostcities.api.ILostCityInformation;
import mcjty.lostsouls.commands.ModCommands;
import mcjty.lostsouls.data.LostChunkData;
import mcjty.lostsouls.data.LostSoulData;
import mcjty.lostsouls.setup.Config;
import mcjty.lostsouls.setup.ModSetup;
import mcjty.lostsouls.varia.ChunkCoord;
import mcjty.lostsouls.varia.Tools;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
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
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

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
    public void onPlayerInteract(PlayerInteractEvent event) {
        Level world = event.getWorld();
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
                            event.getPlayer().sendMessage(new TextComponent(ChatFormatting.YELLOW + Config.MESSAGE_UNSAFE_BUILDING.get()), Util.NIL_UUID);
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
            ChunkCoord chunkCoord = new ChunkCoord(player.getLevel().dimension(), chunkX, chunkZ);
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


            ILostCityInformation info = ModSetup.lostCities.getLostInfo(player.getLevel());
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
            Level world = player.getLevel();
            Random rand = world.getRandom();
            LostChunkData data = LostSoulData.getSoulData(world, chunkX, chunkZ, lost);
            if (isHaunted(data, buildingType)) {
                if (entered) {
                    data.enterBuilding();
                    LostSoulData.getData(world).setDirty();
                    int enteredCount = data.getEnteredCount();
                    if (enteredCount == 1 && Config.ANNOUNCE_ENTER.get()) {
                        // First time
                        player.sendMessage(new TextComponent(ChatFormatting.YELLOW + Config.MESSAGE_BUILDING_HAUNTED.get()), Util.NIL_UUID);
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

                    if (world.getBlockState(new BlockPos(x, y - 1, z)).isAir()) {
                        y--;
                    }
                    if (!world.getBlockState(new BlockPos(x, y, z)).isAir()) {
                        y++;
                    }
                    if (world.getBlockState(new BlockPos(x, y, z)).isAir() && !world.getBlockState(new BlockPos(x, y - 1, z)).isAir()) {
                        double distance = Math.sqrt(position.distToCenterSqr((int) x, (int) y, (int) z));
                        if (distance >= Config.MIN_SPAWN_DISTANCE.get()) {
                            String mob = Tools.getRandomFromList(rand, Config.getRandomMobs());
                            EntityType<?> type = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(mob));
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
                                        boostEntity(world, (LivingEntity) entity);

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

    private void executeCommands(ServerPlayer player, Level world, List<? extends String> commands) {
        if (commands.size() > 0) {
            // @todo 1.18
//            CommandSenderWrapper sender = new CommandSenderWrapper(player, player.getPositionVector(), player.getPosition(), 4, player, null) {
//                @Override
//                public boolean canUseCommand(int permLevel, String commandName) {
//                    return true;
//                }
//            };
//            MinecraftServer server = world.getMinecraftServer();
//            for (String cmd : commands) {
//                server.commandManager.executeCommand(sender, cmd);
//            }
        }
    }

    private void boostEntity(Level world, LivingEntity entity) {
        AttributeInstance entityAttribute = entity.getAttribute(Attributes.MAX_HEALTH);
        Random rand = world.getRandom();
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

        for (Pair<Float, String> pair : Config.getRandomEffects()) {
            if (rand.nextFloat() < pair.getLeft()) {
                String s = pair.getRight();
                String[] split = StringUtils.split(s, ',');
                MobEffect value = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(split[0]));
                if (value == null) {
                    throw new RuntimeException("Cannot find potion effect '" + split[0] + "'!");
                }
                int amplitude = Integer.parseInt(split[1]);
                entity.addEffect(new MobEffectInstance(value, 10000, amplitude));
            }
        }

        String weapon = Tools.getRandomFromList(rand, Config.getRandomWeapons());
        if (!"null".equals(weapon)) {
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(weapon));
            if (item != null) {
                entity.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(item));
            }
        }
        String helmet = Tools.getRandomFromList(rand, Config.getRandomHelmets());
        if (!"null".equals(helmet)) {
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(helmet));
            if (item != null) {
                entity.setItemSlot(EquipmentSlot.HEAD, new ItemStack(item));
            }
        }
        String chestplate = Tools.getRandomFromList(rand, Config.getRandomChests());
        if (!"null".equals(chestplate)) {
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(chestplate));
            if (item != null) {
                entity.setItemSlot(EquipmentSlot.CHEST, new ItemStack(item));
            }
        }
        String leggings = Tools.getRandomFromList(rand, Config.getRandomLeggings());
        if (!"null".equals(leggings)) {
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(leggings));
            if (item != null) {
                entity.setItemSlot(EquipmentSlot.LEGS, new ItemStack(item));
            }
        }
        String boots = Tools.getRandomFromList(rand, Config.getRandomBoots());
        if (!"null".equals(boots)) {
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(boots));
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
                    String[] split = StringUtils.split(tag, '/');
                    try {
                        String dim = split[1];
                        int x = Integer.parseInt(split[2]);
                        int z = Integer.parseInt(split[3]);
                        // Should be in the cache, so we don't need a provider
                        LostChunkData data = LostSoulData.getSoulData(event.getEntity().getLevel(), x, z, null);
                        data.newKill();
                        if (Config.ANNOUNCE_CLEARED.get()) {
                            if (data.getNumberKilled() == data.getTotalMobs()) {
                                source.sendMessage(new TextComponent(ChatFormatting.GREEN + Config.MESSAGE_BUILDING_CLEARED.get()), Util.NIL_UUID);
                                executeCommands(player, source.getLevel(), Config.COMMAND_CLEARED.get());
                            } else if (data.getNumberKilled() == data.getTotalMobs() / 2) {
                                source.sendMessage(new TextComponent(ChatFormatting.YELLOW + Config.MESSAGE_BUILDING_HALFWAY.get()), Util.NIL_UUID);
                            }
                        }
                        LostSoulData.getData(event.getEntity().getLevel()).setDirty();
                    } catch (NumberFormatException e) {
                        LostSouls.logger.error("ForgeEventHandlers.onKill ERROR", e);
                    }
                    return;
                }
            }
        }
    }

}
