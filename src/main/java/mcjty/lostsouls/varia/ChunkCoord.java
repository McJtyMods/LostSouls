package mcjty.lostsouls.varia;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public record ChunkCoord(ResourceKey<Level> dimension, int chunkX,
                         int chunkZ) {

    public ChunkCoord offset(int dx, int dz) {
        return new ChunkCoord(dimension, chunkX + dx, chunkZ + dz);
    }

    @Override
    public String toString() {
        return "ChunkCoord{" +
                "dimension=" + dimension +
                ", chunkX=" + chunkX +
                ", chunkZ=" + chunkZ +
                '}';
    }
}
