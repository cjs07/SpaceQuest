package com.deepwelldevelopment.spacequest.world.chunk;

import com.deepwelldevelopment.spacequest.SpaceQuest;
import com.deepwelldevelopment.spacequest.ThreadManager;
import com.deepwelldevelopment.spacequest.world.World;
import com.deepwelldevelopment.spacequest.world.generation.Generator;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectCollection;

import static java.lang.Math.*;

public class ChunkProvider implements Runnable {

    World world;
    final Long2ObjectMap<Chunk> chunks;
    Generator generator;

    int maxChunks = 2;

    public ChunkProvider(World world, Generator generator, Chunk originChunk) {
        this.world = world;
        this.generator = generator;
        chunks = new Long2ObjectOpenHashMap<>(8192);
        chunks.put(asLong(0, 0), originChunk);
    }

    public boolean isChunkGenerated(int x, int z) {
        try {
            Chunk chunk = chunks.get(asLong(x, z));
            if (chunk == null) {
                return false;
            }
        } catch (IndexOutOfBoundsException e) {
            return false;
        }
        return true;
    }

    public Chunk getChunk(int x, int z) {
        long l = asLong(x, z);
        if (chunks.containsKey(l)) {
            return chunks.get(l);
        } else {
            return generateChunk(x, z);
        }
    }

    public Chunk generateChunk(int x, int z) {
        Chunk c = generator.generateChunk(x, z);
        chunks.put(asLong(x, z), c);
        return c;
    }

    private int xpos = -1;
    private int zpos = 1;
    private double inc = PI/2;
    private int i = 0;
    private int j = 0;
    private int s = 3;

    public void generateChunk() {
        if (i < 4) {
            int xoff = (int) round(cos(i * inc));
            int yoff = (int) round(-sin(i * inc));
            if (j < s-1) {
                xpos += xoff;
                zpos += yoff;
                generateChunk(xpos, zpos);
                j++;
            } else {
                j = 0;
                i++;
            }
        } else {
            i = 0;
            j = 0;
            xpos -= 1;
            zpos -= 1;
            s += 2;
        }
    }

    public ObjectCollection<Chunk> getLoadedChunks() {
//        System.out.println("loaded chunks: " + chunks.size());
        synchronized (chunks) {
            return chunks.values();
        }
    }

    public static long asLong(int x, int z)
    {
        return (long)x & 4294967295L | ((long)z & 4294967295L) << 32;
    }


    @Override
    public void run() {
        int xpos = -1;
        int zpos = -1;
        double inc = PI / 2;
        int s = 3;
        int generatedChunks = 0;
        while (true) {
            for (int i = 0; i < 4; i++) {
                int xoff = (int) round(cos(i * inc));
                int zoff = (int) round(sin(i * inc));
                for (int j = 0; j < s - 1; j++) {
                    xpos += xoff;
                    zpos += zoff;
                    generateChunk(xpos, zpos);
                    generatedChunks++;
                    if (generatedChunks >= SpaceQuest.MAX_CHUNKS-1) {
                        return;
                    }
                    if (!ThreadManager.INSTANCE.isRunning()) {
                        return;
                    }
                }
            }
            xpos -= 1;
            zpos -= 1;
            s += 2;
        }
    }
}