package com.deepwelldevelopment.spacequest.world;

import com.deepwelldevelopment.spacequest.ThreadManager;
import com.deepwelldevelopment.spacequest.block.Block;
import com.deepwelldevelopment.spacequest.event.Event;
import com.deepwelldevelopment.spacequest.event.PlayerClickEvent;
import com.deepwelldevelopment.spacequest.world.chunk.Chunk;
import com.deepwelldevelopment.spacequest.world.chunk.ChunkProvider;
import com.deepwelldevelopment.spacequest.world.generation.Generator;

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

    public void dispatchEvent(Event event) {
        event.dispatch();
    }

    public Block getBlock(int x, int y, int z) {
        int chunkX  = x / 16;
        int chunkZ = z / 16;
        Chunk chunk = provider.getChunk(chunkX, chunkZ);
        if (chunk != null && y >= 0) {
            int posX = (((x % 16) + 16) % 16);
            int posZ = (((z % 16) + 16) % 16);
            return chunk.getLayer(y).getBlock(posX, posZ);
        }
        return null;
    }

    public void setBlock(int x, int y, int z, Block b) {
        int chunkX  = x / 16;
        int chunkZ = z / 16;
        Chunk chunk = provider.getChunk(chunkX, chunkZ);
        int posX = (((x % 16) + 16) % 16);
        int posZ = (((z % 16) + 16) % 16);
        chunk.getLayer(y).setBlock(posX, posZ, b);
        chunk.update();
    }

    //0 = left click, 1 = right click, 2 = middle click ?
    public void playerClicked(int mouseCode, boolean dispatch) {
        PlayerClickEvent event = new PlayerClickEvent(this, mouseCode);
        if (dispatch) {
            dispatchEvent(event);
        }
    }

    public void cleanup() {
        for (Chunk c : provider.getLoadedChunks()) {
            c.cleanup();
        }
    }
}
