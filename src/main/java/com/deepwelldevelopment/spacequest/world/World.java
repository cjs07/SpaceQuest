package com.deepwelldevelopment.spacequest.world;

import com.deepwelldevelopment.spacequest.world.chunk.Chunk;
import com.deepwelldevelopment.spacequest.world.chunk.ChunkProvider;
import com.deepwelldevelopment.spacequest.world.generation.Generator;
import com.deepwelldevelopment.spacequest.ThreadManager;

public class World {

    Generator generator;

    ChunkProvider provider;

    public World() {
        generator = new Generator(this);
        Chunk origin = generator.generateChunk(0, 0);
        provider = new ChunkProvider(this, generator, origin);

        Thread generationThread = new Thread(provider);
        generationThread.start();
        ThreadManager.INSTANCE.attachGenerationThread(generationThread);
    }

    public ChunkProvider getChunkProvider() {
        return provider;
    }

    public void initBlocks() {
        provider.getLoadedChunks().forEach(Chunk::initBlocks);
    }

    public void render() {
        for (Chunk c : provider.getLoadedChunks()) {
            c.render();
        }
    }

    public void cleanup() {
        for (Chunk c : provider.getLoadedChunks()) {
            c.cleanup();
        }
    }
}
