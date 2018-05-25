package com.deepwelldevelopment.spacequest.world;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.deepwelldevelopment.spacequest.block.Block;
import com.deepwelldevelopment.spacequest.block.IBlockProvider;
import com.deepwelldevelopment.spacequest.util.PositionUtils;
import com.deepwelldevelopment.spacequest.world.biome.Biome;
import com.deepwelldevelopment.spacequest.world.biome.IBiomeProvider;
import com.deepwelldevelopment.spacequest.world.chunk.Chunk;
import com.deepwelldevelopment.spacequest.world.chunk.IChunkProvider;
import com.deepwelldevelopment.spacequest.world.noise.SimplexNoise;
import com.deepwelldevelopment.spacequest.world.noise.SimplexNoise2;
import com.deepwelldevelopment.spacequest.world.noise.SimplexNoise3;

import java.util.Iterator;
import java.util.Random;

public class World {

    private static final int MAX_CHUNKS_PER_UPDATE = Runtime.getRuntime().availableProcessors() * 4;
    private static final Object priorityListSync = new Object();
    public static int WIDTH = 16;
    public static int HEIGHT = 128;
    public static long SEED;
    public static int CHUNKDISTANCE = 4;
    public static int GROUND_HEIGHT = 10;
    public static boolean playerIsInWater;
    private static Array<Chunk> chunksWaitingForUpdate = new Array<>();
    private static Array<Chunk> chunksUpdatePriorityList = new Array<>();
    private static IChunkProvider chunkProvider;
    private static IBiomeProvider biomeProvider;
    private static IBlockProvider blockProvider;
    private static Vector3 tmp = new Vector3();
    private static Vector3 tmp2 = new Vector3();
    private final Vector3 previousCameraPosition = new Vector3();

    public World(IBlockProvider blockProvider, IChunkProvider chunkProvider, IBiomeProvider biomeProvider, int width,
                 int height, int viewRange) {
        World.blockProvider = blockProvider;
        World.chunkProvider = chunkProvider;
        World.biomeProvider = biomeProvider;
        WIDTH = width;
        HEIGHT = height;
        CHUNKDISTANCE = viewRange;

        if (SEED == 0) {
            SEED = new Random().nextLong();
        }
        System.out.println("Seed is " + SEED);
        SimplexNoise.init(SEED);
        SimplexNoise2.init(SEED / 10);
        SimplexNoise3.init(SEED / 100);
    }

    public World(IBlockProvider blockProvider, IChunkProvider chunkProvider, IBiomeProvider biomeProvider) {
        World.blockProvider = blockProvider;
        World.chunkProvider = chunkProvider;
        World.biomeProvider = biomeProvider;
    }

    public static Chunk findChunk(int x, int z) {
        return chunkProvider.getChunkAt(PositionUtils.hashOfPosition(x, z));
    }

    public static Biome findBiome(int x, int z) {
        return biomeProvider.getBiomeAt(x, z);
    }

    public static void notifyNeighborsAboutLightChange(int chunkPosX, int chunkPosZ, boolean force) {
        resetLightOnChunk(findChunk(chunkPosX + 1, chunkPosZ), force);
        resetLightOnChunk(findChunk(chunkPosX - 1, chunkPosZ), force);
        resetLightOnChunk(findChunk(chunkPosX, chunkPosZ + 1), force);
        resetLightOnChunk(findChunk(chunkPosX, chunkPosZ - 1), force);
    }

    private static void resetLightOnChunk(Chunk chunk, boolean force) {
        if (chunk != null) {
            chunk.resetLight(force);
        }
    }

    public static void setBlock(float x, float y, float z, Block block, boolean updateLight) {
        Chunk chunk = World.findChunk((int) Math.floor(x / World.WIDTH), (int) Math.floor(z / World.WIDTH));
        if (chunk != null) {
            int localX = (int) x & 15;
            int localY = (int) y;
            int localZ = (int) z & 15;
            chunk.setBlock(localX, localY, localZ, block, updateLight);
            try {
                getBlock((int) x + 1, (int) y, (int) z).onNeighborBlockChange((int) x + 1, (int) y, (int) z);
                getBlock((int) x + 1, (int) y, (int) z - 1).onNeighborBlockChange((int) x + 1, (int) y, (int) z - 1);
                getBlock((int) x + 1, (int) y, (int) z + 1).onNeighborBlockChange((int) x + 1, (int) y, (int) z + 1);
                getBlock((int) x - 1, (int) y, (int) z).onNeighborBlockChange((int) x - 1, (int) y, (int) z);
                getBlock((int) x - 1, (int) y, (int) z - 1).onNeighborBlockChange((int) x - 1, (int) y, (int) z - 1);
                getBlock((int) x - 1, (int) y, (int) z + 1).onNeighborBlockChange((int) x - 1, (int) y, (int) z + 1);
                getBlock((int) x, (int) y, (int) z + 1).onNeighborBlockChange((int) x, (int) y, (int) z + 1);
                getBlock((int) x, (int) y, (int) z - 1).onNeighborBlockChange((int) x, (int) y, (int) z - 1);
            } catch (NullPointerException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void postChunkPriorityUpdate(Chunk chunk) {
        synchronized (priorityListSync) {
            chunksUpdatePriorityList.add(chunk);
        }
    }

    public static Block getBlock(int x, int y, int z) {
        Chunk chunk = World.findChunk((int) Math.floor(tmp.x / World.WIDTH), (int) Math.floor(tmp.z / World.WIDTH));
        if (chunk != null) {
            byte block = chunk.getBlock(x, y, z);
            Block blockById = blockProvider.getBlockById(block);
            return blockById;
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
            if (chunksWaitingForUpdate.size == 0) {
                ArrayMap.Values<Chunk> allChunks = chunkProvider.getAllChunks();
                for (Chunk chunk : allChunks) {
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

        int radius = 16 * CHUNKDISTANCE;
        for (int xc = -radius; xc <= radius; xc += WIDTH) {
            for (int zc = -radius; zc <= radius; zc += WIDTH) {
                if (xc == 0 && zc == 0) continue;
                if (xc * xc + zc * zc <= radius * radius) {
                    checkAndCreateChunk(camPos, xc, zc);
                }
            }
        }
    }

    private void checkAndCreateChunk(Vector3 camPos, int xc, int zc) {
        Chunk chunk = findChunk((int) Math.floor((camPos.x + xc) / WIDTH), (int) Math.floor((camPos.z + zc) / WIDTH));
        if (chunk == null) {
            Vector3 worldPosition = new Vector3((int) Math.floor((camPos.x + xc) / WIDTH) * WIDTH, 0, (int) Math.floor((camPos.z + zc) / WIDTH) * WIDTH);
            int x2 = (int) Math.floor(worldPosition.x / WIDTH);
            int z2 = (int) Math.floor(worldPosition.z / WIDTH);
            chunkProvider.createChunk(worldPosition, x2, z2);
        }
    }

    public ArrayMap.Values<Chunk> getChunks() {
        return chunkProvider.getAllChunks();
    }

    public boolean isPlayerInWater(Camera camera) {
        tmp.set(camera.position.x, camera.position.y + 0.3f, camera.position.z);

        int x = (int) Math.floor(tmp.x);
        int y = (int) tmp.y;
        int z = (int) Math.floor(tmp.z);

        Chunk chunk = World.findChunk((int) Math.floor(tmp.x / World.WIDTH), (int) Math.floor(tmp.z / World.WIDTH));
        if (chunk != null) {
            byte block1 = chunk.getBlock(x, y, z);
            return isBlockLiquid(blockProvider.getBlockById(block1));
        }
//        PhysicsController.setPlayerInWater(false);
        return false;
    }

    private boolean isBlockLiquid(Block block) {
        return block.isLiquid();
    }
}
