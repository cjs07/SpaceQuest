package com.deepwelldevelopment.spacequest.world.chunk;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.ArrayMap.Values;
import com.deepwelldevelopment.spacequest.block.IBlockProvider;
import com.deepwelldevelopment.spacequest.util.PositionUtils;
import com.deepwelldevelopment.spacequest.world.World;

public class OverworldChunkProvider implements IChunkProvider {

    private static ArrayMap<Long, Chunk> chunks = new ArrayMap<>();
    private final IBlockProvider blockProvider;
    //    private final IBiomeProvider biomeProvider;
    private final World world;

    //TODO: add biomeProvider
    public OverworldChunkProvider(World world, IBlockProvider blockProvider) {
        this.world = world;
        this.blockProvider = blockProvider;
    }


    @Override
    public Chunk getChunkAt(int x, int y, int z) {
        return null;
    }

    @Override
    public Chunk getChunkAt(long position) {
        return chunks.get(position);
    }

    @Override
    public Values<Chunk> getAllChunks() {
        return chunks.values();
    }

    @Override
    public void createChunk(Vector3 worldPosition, int x, int z) {
//        Biome biome = biomeProvider.getBiomeAt(x, z);
        Chunk c = new Chunk(world, blockProvider, worldPosition, x, z);
        chunks.put(PositionUtils.hashOfPosition(x, z), c);
    }
}
