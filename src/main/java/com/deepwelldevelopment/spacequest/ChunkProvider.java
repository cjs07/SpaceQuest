package com.deepwelldevelopment.spacequest;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectCollection;

public class ChunkProvider implements Runnable {

    World world;
    Long2ObjectMap<Chunk> chunks;
    Generator generator;

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

    public ObjectCollection<Chunk> getLoadedChunks() {
        System.out.println("loaded chunks: " + chunks.size());
        return chunks.values();
    }

    public static long asLong(int x, int z)
    {
        return (long)x & 4294967295L | ((long)z & 4294967295L) << 32;
    }


    @Override
    public void run() {
        int squareSize = 3;
        while (true) {
            for (int x = -squareSize/2; x < squareSize/2; x++) {
                for (int z = -squareSize / 2; z < squareSize / 2; z++){
                    if (!isChunkGenerated(x, z)) {
                        long l = asLong(x, z);
                        chunks.put(l, generator.generateChunk(x, z));
                    }
                }
            }
            squareSize += 2;
        }
    }
}