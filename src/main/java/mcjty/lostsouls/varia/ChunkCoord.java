package mcjty.lostsouls.varia;

import mcjty.lostcities.worldgen.lost.Orientation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public record ChunkCoord(ResourceKey<Level> dimension, int chunkX,
                         int chunkZ) {

    public mcjty.lostcities.varia.ChunkCoord lower(Orientation o) {
        return switch (o) {
            case X -> new mcjty.lostcities.varia.ChunkCoord(dimension, chunkX - 1, chunkZ);
            case Z -> new mcjty.lostcities.varia.ChunkCoord(dimension, chunkX, chunkZ - 1);
        };
    }

    public mcjty.lostcities.varia.ChunkCoord higher(Orientation o) {
        return switch (o) {
            case X -> new mcjty.lostcities.varia.ChunkCoord(dimension, chunkX + 1, chunkZ);
            case Z -> new mcjty.lostcities.varia.ChunkCoord(dimension, chunkX, chunkZ + 1);
        };
    }

    public int getCoord(Orientation o) {
        return switch (o) {
            case X -> chunkX;
            case Z -> chunkZ;
        };
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
