package mcjty.lostsouls.varia;

public class ChunkCoord {
    private final int dimension;
    private final int chunkX;
    private final int chunkZ;

    public ChunkCoord(int dimension, int chunkX, int chunkZ) {
        this.dimension = dimension;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
    }

    public int getDimension() {
        return dimension;
    }

    public int getChunkX() {
        return chunkX;
    }

    public int getChunkZ() {
        return chunkZ;
    }

    @Override
    public String toString() {
        return "ChunkCoord{" +
                "dimension=" + dimension +
                ", chunkX=" + chunkX +
                ", chunkZ=" + chunkZ +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ChunkCoord that = (ChunkCoord) o;

        if (dimension != that.dimension) return false;
        if (chunkX != that.chunkX) return false;
        if (chunkZ != that.chunkZ) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = dimension;
        result = 31 * result + chunkX;
        result = 31 * result + chunkZ;
        return result;
    }
}
