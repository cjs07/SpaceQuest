package com.deepwelldevelopment.spacequest.world.chunk;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ArrayMap;

public interface IChunkProvider {

    Chunk getChunkAt(int x, int y, int z);

    Chunk getChunkAt(long position);

    ArrayMap.Values<Chunk> getAllChunks();

    void createChunk(Vector3 worldPosition, int x, int z);
}
