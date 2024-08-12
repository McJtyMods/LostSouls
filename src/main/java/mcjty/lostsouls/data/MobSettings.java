package mcjty.lostsouls.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class MobSettings {

    public record RL(ResourceLocation name, float weight) {
    }

    public record Effect(ResourceLocation name, int level, float weight) {
    }

    public record Range<T>(T min, T max) {
    }

    public static final Codec<RL> RL_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ResourceLocation.CODEC.fieldOf("name").forGetter(RL::name),
                    Codec.FLOAT.fieldOf("weight").forGetter(RL::weight)
            ).apply(instance, RL::new));

    public static final Codec<Effect> EFFECT_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ResourceLocation.CODEC.fieldOf("name").forGetter(Effect::name),
                    Codec.INT.fieldOf("level").forGetter(Effect::level),
                    Codec.FLOAT.fieldOf("weight").forGetter(Effect::weight)
            ).apply(instance, Effect::new));

    public static final Codec<Range<Double>> DOUBLE_RANGE_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.DOUBLE.fieldOf("min").forGetter(Range::min),
                    Codec.DOUBLE.fieldOf("max").forGetter(Range::max)
            ).apply(instance, Range::new));

    public static final Codec<Range<Integer>> INT_RANGE_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("min").forGetter(Range::min),
                    Codec.INT.fieldOf("max").forGetter(Range::max)
            ).apply(instance, Range::new));

    public static final Codec<MobSettings> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ResourceLocation.CODEC.listOf().fieldOf("buildings").forGetter(s -> new ArrayList<>(s.buildings)),
                    ResourceLocation.CODEC.listOf().fieldOf("multibuildings").forGetter(s -> new ArrayList<>(s.multiBuildings)),
                    RL_CODEC.listOf().optionalFieldOf("mobs").forGetter(l -> Optional.ofNullable(l.mobs)),
                    RL_CODEC.listOf().optionalFieldOf("weapons").forGetter(l -> Optional.ofNullable(l.weapons)),
                    RL_CODEC.listOf().optionalFieldOf("helmets").forGetter(l -> Optional.ofNullable(l.helmets)),
                    RL_CODEC.listOf().optionalFieldOf("chestplates").forGetter(l -> Optional.ofNullable(l.chestplates)),
                    RL_CODEC.listOf().optionalFieldOf("leggings").forGetter(l -> Optional.ofNullable(l.leggings)),
                    RL_CODEC.listOf().optionalFieldOf("boots").forGetter(l -> Optional.ofNullable(l.boots)),
                    EFFECT_CODEC.listOf().optionalFieldOf("effects").forGetter(l -> Optional.ofNullable(l.effects)),
                    DOUBLE_RANGE_CODEC.optionalFieldOf("healthbonus").forGetter(l -> Optional.ofNullable(l.healthBonus)),
                    DOUBLE_RANGE_CODEC.optionalFieldOf("damagebonus").forGetter(l -> Optional.ofNullable(l.damageBonus)),
                    INT_RANGE_CODEC.optionalFieldOf("mobamounts").forGetter(l -> Optional.ofNullable(l.mobAmounts)),
                    Codec.DOUBLE.optionalFieldOf("hauntedchance").forGetter(l -> Optional.ofNullable(l.hauntedChance))
            ).apply(instance, MobSettings::new));

    private final Set<ResourceLocation> buildings;
    private final Set<ResourceLocation> multiBuildings;

    private final List<RL> mobs;
    private final List<RL> weapons;
    private final List<RL> helmets;
    private final List<RL> chestplates;
    private final List<RL> leggings;
    private final List<RL> boots;
    private final List<Effect> effects;
    private final Range<Double> healthBonus;
    private final Range<Double> damageBonus;
    private final Range<Integer> mobAmounts;
    private final Double hauntedChance;

    public MobSettings(
            List<ResourceLocation> buildings,
            List<ResourceLocation> multiBuildings,
            Optional<List<RL>> mobs, Optional<List<RL>> weapons, Optional<List<RL>> helmets,
            Optional<List<RL>> chestplates, Optional<List<RL>> leggings, Optional<List<RL>> boots,
            Optional<List<Effect>> effects, Optional<Range<Double>> healthBonus,
            Optional<Range<Double>> damageBonus, Optional<Range<Integer>> mobAmounts, Optional<Double> hauntedChance) {
        this.buildings = new HashSet<>(buildings);
        this.multiBuildings = new HashSet<>(multiBuildings);
        this.mobs = mobs.orElse(null);
        this.weapons = weapons.orElse(null);
        this.helmets = helmets.orElse(null);
        this.chestplates = chestplates.orElse(null);
        this.leggings = leggings.orElse(null);
        this.boots = boots.orElse(null);
        this.effects = effects.orElse(null);
        this.healthBonus = healthBonus.orElse(null);
        this.damageBonus = damageBonus.orElse(null);
        this.mobAmounts = mobAmounts.orElse(null);
        this.hauntedChance = hauntedChance.orElse(null);
    }

    @Nonnull
    public Set<ResourceLocation> getBuildings() {
        return buildings;
    }

    @Nonnull
    public Set<ResourceLocation> getMultiBuildings() {
        return multiBuildings;
    }

    @Nullable
    public List<RL> getMobs() {
        return mobs;
    }

    @Nullable
    public List<RL> getHelmets() {
        return helmets;
    }

    @Nullable
    public List<RL> getWeapons() {
        return weapons;
    }

    @Nullable
    public List<RL> getChestplates() {
        return chestplates;
    }

    @Nullable
    public List<RL> getLeggings() {
        return leggings;
    }

    @Nullable
    public List<RL> getBoots() {
        return boots;
    }

    @Nullable
    public List<Effect> getEffects() {
        return effects;
    }

    @Nullable
    public Range<Double> getHealthBonus() {
        return healthBonus;
    }

    @Nullable
    public Range<Double> getDamageBonus() {
        return damageBonus;
    }

    @Nullable
    public Range<Integer> getMobAmounts() {
        return mobAmounts;
    }

    @Nullable
    public Double getHauntedChance() {
        return hauntedChance;
    }

    public static MobSettings merge(MobSettings base, MobSettings override) {
        return new MobSettings(
                new ArrayList<>(override.buildings),        // The base doesn't have buildings so we always take from the override
                new ArrayList<>(override.multiBuildings),
                Optional.of(override.mobs == null ? base.mobs : override.mobs),
                Optional.of(override.weapons == null ? base.weapons : override.weapons),
                Optional.of(override.helmets == null ? base.helmets : override.helmets),
                Optional.of(override.chestplates == null ? base.chestplates : override.chestplates),
                Optional.of(override.leggings == null ? base.leggings : override.leggings),
                Optional.of(override.boots == null ? base.boots : override.boots),
                Optional.of(override.effects == null ? base.effects : override.effects),
                Optional.of(override.healthBonus == null ? base.healthBonus : override.healthBonus),
                Optional.of(override.damageBonus == null ? base.damageBonus : override.damageBonus),
                Optional.of(override.mobAmounts == null ? base.mobAmounts : override.mobAmounts),
                Optional.ofNullable(override.hauntedChance == null ? base.hauntedChance : override.hauntedChance)
        );
    }
}
