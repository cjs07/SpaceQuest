package com.deepwelldevelopment.spacequest;

public class World {

    Chunk[][] chunks;
    Generator generator;

    public World() {
        chunks = new Chunk[3][3];
        generator = new Generator(this);

        for (int x = 0; x < chunks.length; x++) {
            for (int z = 0; z < chunks[0].length; z++) {
                chunks[x][z] = generator.generateChunk(x, z);
            }
        }
    }

    public void render() {
        for (Chunk[] chunks1 : chunks) {
            for (Chunk chunk : chunks1) {
                chunk.render();
            }
        }
    }

    public void cleanup() {
        for (Chunk[] chunks1 : chunks) {
            for (Chunk chunk : chunks1) {
                chunk.cleanup();
            }
        }
    }
}
