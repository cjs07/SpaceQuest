package com.deepwelldevelopment.spacequest;

public class World {

    Generator generator;
    ChunkProvider chunkProvider;

    Thread generationThread;

    Chunk[][] chunks;

    public World() {
        generator = new Generator(this);
        Chunk originChunk = generator.generateChunk(0, 0);
        chunkProvider = new ChunkProvider(this, generator, originChunk);

        System.out.println("Starting world generation in a new thread (This is using highly unoptimized chunk selection to generate chunks, and never stops " +
                "(THIS WILL LAG EVENTUALLY. YOU HAVE BEEN WARNED)");
        generationThread = new Thread(chunkProvider);
        ThreadManager.INSTANCE.attachGenerationThread(generationThread);
//        generationThread.start();

        chunks = new Chunk[1][1];
        chunks[0][0] = originChunk;
    }

    public void render() {
//        for (Chunk chunk : chunkProvider.getLoadedChunks()) {
//            if (chunk != null) {
//                chunk.render();
//            }
//        }
        chunks[0][0].render();
    }

    public void cleanup() {
        for (Chunk chunk : chunkProvider.getLoadedChunks()) {
            if (chunk != null) {
                chunk.render();
            }
        }
    }
}
