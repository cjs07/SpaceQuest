package com.deepwelldevelopment.spacequest.world;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.deepwelldevelopment.spacequest.block.Block;
import com.deepwelldevelopment.spacequest.block.BlockProvider;
import com.deepwelldevelopment.spacequest.block.IBlockProvider;
import com.deepwelldevelopment.spacequest.util.PositionUtils;
import com.deepwelldevelopment.spacequest.world.biome.Biome;
import com.deepwelldevelopment.spacequest.world.biome.IBiomeProvider;
import com.deepwelldevelopment.spacequest.world.chunk.Chunk;
import com.deepwelldevelopment.spacequest.world.chunk.IChunkProvider;
import com.deepwelldevelopment.spacequest.world.chunk.OverworldChunkProvider;

import java.util.Iterator;
import java.util.Random;

public class World {

    public static final int CHUNK_WIDTH = 16;
    public static final int MAX_HEIGHT = 256;
    public static final int CHUNK_DISTANCE = 4;
    public static final int GROUND_HEIGHT = 10;
    private static final int MAX_CHUNKS_PER_UPDATE = Runtime.getRuntime().availableProcessors() * 4;
    private static final Object priorityListSync = new Object();
    private final Vector3 previousCameraPosition = new Vector3();
    //TODO: player in water
    private long seed;
    private Array<Chunk> chunksWaitingForUpdate = new Array<>();
    private Array<Chunk> chunksUpdatePriorityList = new Array<>();
    private IChunkProvider chunkProvider;
    private IBiomeProvider biomeProvider;
    private IBlockProvider blockProvider;
    private Vector3 tmp = new Vector3();
    private Vector3 tmp2 = new Vector3();

    public World(IBlockProvider blockProvider, IBiomeProvider biomeProvider) {
        this.blockProvider = blockProvider;
        this.chunkProvider = new OverworldChunkProvider(this, blockProvider, biomeProvider);
        this.biomeProvider = biomeProvider;

        if (seed == 0) {
            seed = new Random().nextLong();
        }
        System.out.println("Seed: " + seed);
        //init noise
    }

    public IChunkProvider getChunkProvider() {
        return chunkProvider;
    }

    public IBiomeProvider getBiomeProvider() {
        return biomeProvider;
    }

    public long getSeed() {
        return seed;
    }

    public IBlockProvider getBlockProvider() {
        return blockProvider;
    }

    public ArrayMap.Values<Chunk> getChunks() {
        return chunkProvider.getAllChunks();
    }

    public Chunk findChunk(int xToFind, int zToFind) {
        return chunkProvider.getChunkAt(PositionUtils.hashOfPosition(xToFind, zToFind));
    }

    public Biome findBiome(int x, int z) {
        return biomeProvider.getBiomeAt(x, z);
    }

    public void notifyNeighborsAboutLightChange(int chunkPosX, int chunkPosZ, boolean force) {
        resetLightOnChunk(findChunk(chunkPosX + 1, chunkPosZ), force);
        resetLightOnChunk(findChunk(chunkPosX - 1, chunkPosZ), force);
        resetLightOnChunk(findChunk(chunkPosX, chunkPosZ + 1), force);
        resetLightOnChunk(findChunk(chunkPosX, chunkPosZ - 1), force);
    }

    private void resetLightOnChunk(Chunk chunk, boolean force) {
        if (chunk != null) {
            chunk.resetLight(force);
        }
    }

    public void setBlock(float x, float y, float z, Block block, boolean updateLight) {
        Chunk chunk = this.findChunk((int) Math.floor(x / CHUNK_WIDTH),
                (int) Math.floor(z / CHUNK_WIDTH)
        );
        if (chunk != null) {
            int localX = (int) x & 15;
            int localZ = (int) z & 15;
            chunk.setBlock(localX, (int) y, localZ, block, updateLight);
            try {
//                getBlock((int) x + 1, (int) y, (int) z)
//                        .onNeighborBlockChange(this, (int) x + 1, (int) y, (int) z);
//                getBlock((int) x + 1, (int) y, (int) z - 1)
//                        .onNeighborBlockChange(this, (int) x + 1, (int) y, (int) z - 1);
//                getBlock((int) x + 1, (int) y, (int) z + 1)
//                        .onNeighborBlockChange(this, (int) x + 1, (int) y, (int) z + 1);
//                getBlock((int) x - 1, (int) y, (int) z)
//                        .onNeighborBlockChange(this, (int) x - 1, (int) y, (int) z);
//                getBlock((int) x - 1, (int) y, (int) z - 1)
//                        .onNeighborBlockChange(this, (int) x - 1, (int) y, (int) z - 1);
//                getBlock((int) x - 1, (int) y, (int) z + 1)
//                        .onNeighborBlockChange(this, (int) x - 1, (int) y, (int) z + 1);
//                getBlock((int) x, (int) y, (int) z + 1)
//                        .onNeighborBlockChange(this, (int) x, (int) y, (int) z + 1);
//                getBlock((int) x, (int) y, (int) z - 1)
//                        .onNeighborBlockChange(this, (int) x, (int) y, (int) z - 1);
//                getBlock((int) x, (int) (y + 1), (int) z)
//                        .onNeighborBlockChange(this, (int) x, (int) y + 1, (int) z);
//                getBlock((int) x, (int) (y - 1), (int) z)
//                        .onNeighborBlockChange(this, (int) x, (int) y - 1, (int) z);
            } catch (NullPointerException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void postChunkPriorityUpdate(Chunk chunk) {
        synchronized (priorityListSync) {
            chunksUpdatePriorityList.add(chunk);
        }
    }

    public Block getBlock(int x, int y, int z) {
        Chunk chunk = this.findChunk((int) Math.floor(tmp.x / World.CHUNK_WIDTH),
                (int) Math.floor(tmp.z / World.CHUNK_WIDTH)
        );
        if (chunk != null) {
            byte block = chunk.getBlock(x, y, z);
            return blockProvider.getBlockById(block);
        }
        return blockProvider.getBlockById((byte) 0);
    }

    public void update(Vector3 camPos) {
        for (Chunk chunk : chunkProvider.getAllChunks()) {
            chunk.tick();
        }

        if (chunksUpdatePriorityList.size > 0) {
            synchronized (priorityListSync) {
                Chunk chunk = chunksUpdatePriorityList.pop();
                chunk.update();
            }
        } else {
            if (chunksUpdatePriorityList.size == 0) {
                ArrayMap.Values<Chunk> alLChunks = chunkProvider.getAllChunks();
                for (Chunk chunk : alLChunks) {
                    if (chunk.isActive()) {
                        chunksWaitingForUpdate.add(chunk);
                    }
                }
            }

            Iterator<Chunk> iterator = chunksWaitingForUpdate.iterator();
            for (int i = 0; i < MAX_CHUNKS_PER_UPDATE; i++) {
                if (iterator.hasNext()) {
                    Chunk chunk = iterator.next();
                    iterator.remove();
                    if (chunk.isActive()) {
                        chunk.update();
                    }
                }
            }
        }

        if (camPos.dst(previousCameraPosition) < 16) {
            return;
        }
        previousCameraPosition.set(camPos);
        checkAndCreateChunk(camPos, 0, 0);

        int radius = 16 * CHUNK_DISTANCE;
        for (int xc = -radius; xc <= radius; xc += CHUNK_WIDTH) {
            for (int zc = -radius; zc <= radius; zc += CHUNK_WIDTH) {
                if (xc == 0 && zc == 0) continue;
                if (xc * xc + zc * zc <= radius * radius) {
                    checkAndCreateChunk(camPos, xc, zc);
                }
            }
        }
    }

    private void checkAndCreateChunk(Vector3 camPos, int xc, int zc) {
        Chunk chunk = findChunk((int) Math.floor((camPos.x + xc) / CHUNK_WIDTH),
                (int) Math.floor((camPos.z + zc) / CHUNK_WIDTH)
        );
        if (chunk == null) {
            Vector3 worldPosition =
                    new Vector3((int) Math.floor((camPos.x + xc) / CHUNK_WIDTH) * CHUNK_WIDTH, 0,
                            (int) Math.floor((camPos.z + zc) / CHUNK_WIDTH) * CHUNK_WIDTH
                    );
            int x2 = (int) Math.floor(worldPosition.x / CHUNK_WIDTH);
            int z2 = (int) Math.floor(worldPosition.z / CHUNK_WIDTH);
            chunkProvider.createChunk(worldPosition, x2, z2);
        }
    }

    //TODO: isPlayerInWater

    //TODO: updateBreakState

    public void breakBlock(int x, int y, int z) {
        setBlock(x, y, z, BlockProvider.air, false);
    }

    //TODO: isBlockLiquid?

    //TODO: blockInteract
}
