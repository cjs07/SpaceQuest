package com.deepwelldevelopment.spacequest.world.generation;

import com.deepwelldevelopment.spacequest.world.chunk.Chunk;
import com.deepwelldevelopment.spacequest.world.World;
import com.flowpowered.noise.module.source.Perlin;

public class Generator {

    World world;

    int noiseSeed;
    Perlin perlin;

    double offsetIncrement = 0.001;

    public Generator(World world) {
        this.world = world;

        noiseSeed = (int) System.nanoTime();
        perlin = new Perlin();

        perlin.setSeed(noiseSeed);
    }

    public Chunk generateChunk(int chunkX, int chunkZ) {
        double xOff = chunkX * 16 * offsetIncrement;
        double  zOff = chunkZ * 16 * offsetIncrement;
        Chunk chunk = new Chunk(world, chunkX, chunkZ);

        for (int x = 0; x < 16; x++) {
            for (int z = 15; z >= 0; z--) {
                double yPercent = perlin.getValue(xOff, 0, zOff);
                zOff -= offsetIncrement;
                chunk.adjustHeight(yPercent, x, z);
            }
            xOff += offsetIncrement;
            System.out.println();
        }
        chunk.generateLayers();
        return chunk;
    }
}
