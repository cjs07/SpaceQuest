package com.deepwelldevelopment.spacequest.world;

import com.deepwelldevelopment.spacequest.world.chunk.Chunk;
import com.deepwelldevelopment.spacequest.world.chunk.ChunkProvider;
import com.deepwelldevelopment.spacequest.world.generation.Generator;

import java.util.Collection;

public class World {

    public static final int MAX_HEIGHT = 256;
    public Chunk origin;
    private Generator generator;
    private ChunkProvider chunkProvider;

    public World() {
        generator = new Generator(this);
        origin = generator.generateChunk(0, 0);
        chunkProvider = new ChunkProvider(this, generator, origin);
    }

    public Collection<Chunk> getChunks() {
        return chunkProvider.getLoadedChunks();
    }
}
