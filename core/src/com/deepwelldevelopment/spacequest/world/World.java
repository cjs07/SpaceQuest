package com.deepwelldevelopment.spacequest.world;

import com.badlogic.gdx.utils.ArrayMap;
import com.deepwelldevelopment.spacequest.world.chunk.Chunk;

public class World {

    public static final int CHUNK_WIDTH = 16;
    public static final int MAX_HEIGHT = 256;
    public static final int CHUNK_DISTANCE = 4;
    public static final int GROUND_HEIGHT = 10;

    public ArrayMap.Values<Chunk> getChunks() {
        return null;
    }

    public Chunk findChunk(int xToFind, int zToFind) {
        return null;
    }
}
