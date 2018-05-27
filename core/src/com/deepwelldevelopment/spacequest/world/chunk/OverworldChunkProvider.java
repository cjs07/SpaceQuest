package com.deepwelldevelopment.spacequest.world.chunk;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ArrayMap;
import com.deepwelldevelopment.spacequest.block.IBlockProvider;
import com.deepwelldevelopment.spacequest.util.PositionUtils;
import com.deepwelldevelopment.spacequest.world.World;
import com.deepwelldevelopment.spacequest.world.biome.Biome;
import com.deepwelldevelopment.spacequest.world.biome.IBiomeProvider;

public class OverworldChunkProvider implements IChunkProvider {

//    final Long2ObjectMap<Chunk> chunks;
//    World world;
//    Generator generator;

    private static ArrayMap<Long, Chunk> chunks = new ArrayMap<Long, Chunk>();
    private final IBlockProvider blockProvider;
    private final IBiomeProvider biomeProvider;
    private final World world;

    public OverworldChunkProvider(World world, IBlockProvider blockProvider, IBiomeProvider biomeProvider) {
        this.world = world;
        this.blockProvider = blockProvider;
        this.biomeProvider = biomeProvider;
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
    public ArrayMap.Values<Chunk> getAllChunks() {
        return chunks.values();
    }

    @Override
    public void createChunk(Vector3 worldPosition, int x, int z) {
        Biome biome = biomeProvider.getBiomeAt(x, z);
        //System.out.println("Got biome "+biome.getClass().getName());
        Chunk c = new Chunk(world, blockProvider, worldPosition, x, z, biome);
        chunks.put(PositionUtils.hashOfPosition(x, z), c);
    }

//    @Override
//    public void run() {
//        try {
//            Thread.sleep(2000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        int xpos = -1;
//        int zpos = -1;
//        double inc = PI / 2;
//        int s = 3;
//        int generatedChunks = 0;
//        while (true) {
//            for (int i = 0; i < 4; i++) {
//                int xoff = (int) round(cos(i * inc));
//                int zoff = (int) round(sin(i * inc));
//                for (int j = 0; j < s - 1; j++) {
//                    xpos += xoff;
//                    zpos += zoff;
//                    generateChunk(xpos, zpos);
////                    SpaceQuest.worldModel.addChunk(generateChunk(xpos, zpos));
//                    System.out.println("chunk generated");
//                    generatedChunks++;
//                    if (generatedChunks >= 10) {
//                        return;
//                    }
//                }
//            }
//            xpos -= 1;
//            zpos -= 1;
//            s += 2;
//        }
//    }
}
