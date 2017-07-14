package mcjty.lostsouls;

import mcjty.lostcities.api.ILostChunkGenerator;
import mcjty.lostcities.api.ILostChunkInfo;
import mcjty.lostsouls.config.Config;
import mcjty.lostsouls.data.LostChunkData;
import mcjty.lostsouls.data.LostSoulData;
import mcjty.lostsouls.varia.Tools;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

public class ForgeEventHandlers {

    public static int timeout = Config.SERVERTICK_TIMEOUT;

    @SubscribeEvent
    public void onTickEvent(TickEvent.ServerTickEvent event) {
        timeout--;
        if (timeout > 0) {
            return;
        }
        timeout = Config.SERVERTICK_TIMEOUT;

        PlayerList list = DimensionManager.getWorld(0).getMinecraftServer().getPlayerList();
        for (EntityPlayerMP player : list.getPlayers()) {
            IChunkGenerator v = ((WorldServer) player.getEntityWorld()).getChunkProvider().chunkGenerator;
            if (v instanceof ILostChunkGenerator) {
                handleSpawn(player, (ILostChunkGenerator) v);
            }
        }
    }

    private boolean isHaunted(int chunkX, int chunkZ, World world, String buildingType) {
        if (Config.getExcludedBuildings().contains(buildingType)) {
            return false;
        }
        LostChunkData data = LostSoulData.getSoulData(world, world.provider.getDimension(), chunkX, chunkZ);
        return data.isHaunted() && data.getNumberKilled() < data.getMaxMobs();
    }

    private void handleSpawn(EntityPlayerMP player, ILostChunkGenerator lost) {
        BlockPos position = player.getPosition();
        int chunkX = position.getX() >> 4;
        int chunkZ = position.getZ() >> 4;
        ILostChunkInfo chunkInfo = lost.getChunkInfo(chunkX, chunkZ);
        String buildingType = chunkInfo.getBuildingType();
        if (buildingType != null) {
            // We have a building
            World world = player.getEntityWorld();
            if (isHaunted(chunkX, chunkZ, world, buildingType)) {
                double x = chunkX * 16 + world.rand.nextDouble() * 16.0;
                double y = (position.getY() + world.rand.nextInt(3) - 1);
                double z = chunkZ * 16 + world.rand.nextDouble() * 16.0;

                if (world.isAirBlock(new BlockPos(x, y - 1, z))) {
                    y--;
                }
                if (!world.isAirBlock(new BlockPos(x, y, z))) {
                    y++;
                }
                if (world.isAirBlock(new BlockPos(x, y, z))) {
                    double distance = position.getDistance((int) x, (int) y, (int) z);
                    if (distance >= Config.MIN_SPAWN_DISTANCE) {
                        String mob = Tools.getRandomFromList(world.rand, Config.getRandomMobs());
                        Entity entity = EntityList.createEntityByIDFromName(new ResourceLocation(mob), world);
                        int cnt = world.getEntitiesWithinAABB(entity.getClass(), (new AxisAlignedBB(x, y, z, x + 1, y + 1, z + 1).grow(8.0))).size();
                        if (cnt <= Config.SPAWN_MAX_NEARBY) {
                            entity.setLocationAndAngles(x, y, z, world.rand.nextFloat() * 360.0F, 0.0F);
                            if (!Config.CHECK_VALID_SPAWN || ((EntityLiving) entity).getCanSpawnHere()) {
                                if (((EntityLiving) entity).isNotColliding()) {
                                    boostEntity(world, (EntityLiving) entity);

                                    entity.addTag("_ls_:" + world.provider.getDimension() + ":" + chunkX + ":" + chunkZ);
                                    world.spawnEntity(entity);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void boostEntity(World world, EntityLiving entity) {
        IAttributeInstance entityAttribute = entity.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH);
        if (entityAttribute != null) {
            float f = world.rand.nextFloat() * (Config.MAX_HEALTH_BONUS - Config.MIN_HEALTH_BONUS) + Config.MIN_HEALTH_BONUS;
            double newMax = entityAttribute.getBaseValue() * f;
            entityAttribute.setBaseValue(newMax);
            entity.setHealth((float) newMax);
        }
        entityAttribute = entity.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
        if (entityAttribute != null) {
            float f = world.rand.nextFloat() * (Config.MAX_DAMAGE_BONUS - Config.MIN_DAMAGE_BONUS) + Config.MIN_DAMAGE_BONUS;
            double newMax = entityAttribute.getBaseValue() * f;
            entityAttribute.setBaseValue(newMax);
        }

        for (Pair<Float, String> pair : Config.getRandomEffects()) {
            if (world.rand.nextFloat() < pair.getLeft()) {
                String s = pair.getRight();
                String[] split = StringUtils.split(s, ',');
                Potion value = ForgeRegistries.POTIONS.getValue(new ResourceLocation(split[0]));
                if (value == null) {
                    throw new RuntimeException("Cannot find potion effect '" + split[0] + "'!");
                }
                int amplitude = Integer.parseInt(split[1]);
                entity.addPotionEffect(new PotionEffect(value, 10000, amplitude));
            }
        }

        String weapon = Tools.getRandomFromList(world.rand, Config.getRandomWeapons());
        if (!"null".equals(weapon)) {
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(weapon));
            if (item != null) {
                entity.setHeldItem(EnumHand.MAIN_HAND, new ItemStack(item));
            }
        }
        String helmet = Tools.getRandomFromList(world.rand, Config.getRandomHelmets());
        if (!"null".equals(helmet)) {
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(helmet));
            if (item != null) {
                entity.setItemStackToSlot(EntityEquipmentSlot.HEAD, new ItemStack(item));
            }
        }
        String chestplate = Tools.getRandomFromList(world.rand, Config.getRandomChests());
        if (!"null".equals(chestplate)) {
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(chestplate));
            if (item != null) {
                entity.setItemStackToSlot(EntityEquipmentSlot.CHEST, new ItemStack(item));
            }
        }
        String leggings = Tools.getRandomFromList(world.rand, Config.getRandomLeggings());
        if (!"null".equals(leggings)) {
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(leggings));
            if (item != null) {
                entity.setItemStackToSlot(EntityEquipmentSlot.LEGS, new ItemStack(item));
            }
        }
        String boots = Tools.getRandomFromList(world.rand, Config.getRandomBoots());
        if (!"null".equals(boots)) {
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(boots));
            if (item != null) {
                entity.setItemStackToSlot(EntityEquipmentSlot.FEET, new ItemStack(item));
            }
        }
    }

    @SubscribeEvent
    public void onKill(LivingDeathEvent event) {
        Entity source = event.getSource().getTrueSource();
        if (source instanceof EntityPlayer) {
            for (String tag : event.getEntity().getTags()) {
                if (tag.startsWith("_ls_:")) {
                    String[] split = StringUtils.split(tag, ':');
                    try {
                        int dim = Integer.parseInt(split[1]);
                        int x = Integer.parseInt(split[2]);
                        int z = Integer.parseInt(split[3]);
                        LostChunkData data = LostSoulData.getSoulData(event.getEntity().world, dim, x, z);
                        data.newKill();
                        if (Config.ANNOUNCE_CLEARED) {
                            if (data.getNumberKilled() == data.getMaxMobs()) {
                                source.sendMessage(new TextComponentString(TextFormatting.GREEN + "The building feels a lot safer now!"));
                            }
                        }
                        LostSoulData.getData(event.getEntity().world).save(event.getEntity().world);
                    } catch (NumberFormatException e) {
                        System.out.println("ForgeEventHandlers.onKill ERROR");
                    }
                    return;
                }
            }
        }
    }

}
