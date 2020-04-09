package com.deepwelldevelopment.spacequest.world.chunk;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.deepwelldevelopment.spacequest.block.Block;
import com.deepwelldevelopment.spacequest.block.BlockProvider;
import com.deepwelldevelopment.spacequest.block.IBlockProvider;
import com.deepwelldevelopment.spacequest.client.render.BoxMesh;
import com.deepwelldevelopment.spacequest.client.render.VoxelMesh;
import com.deepwelldevelopment.spacequest.world.World;
import com.deepwelldevelopment.spacequest.world.biome.Biome;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Chunk {

    public static final byte LIGHT = 1;
    public static final byte DARKNESS = 32;

    private static final ExecutorService executorService =
            Executors.newFixedThreadPool(
                    Runtime.getRuntime().availableProcessors() - 1);

    //TODO: world gen

    private final IBlockProvider blockProvider;
    private final byte[] map;
    private final Vector3 worldPosition;
    private final int chunkPosX;
    private final int chunkPosZ;
    private final Object syncToken = new Object();

    private Array<VoxelMesh> meshes = new Array<>();
    private Array<VoxelMesh> alphaMeshes = new Array<>();
    private World world;
    private boolean ready;
    private byte[] lightmap;
    //true if the block at a given position can see the sky (and is thus the highest in the column)
    private boolean[] heightmap;
    private int[][] generationHeightmap;
    private Biome biome;
    private int blockCounter;
    private boolean active = true;
    private boolean recalculating;
    //TODO: noise caches
    private boolean needLightUpdate = true;
    private boolean fullRebuildOfLight;
    private boolean needMeshUpdate;
    private int timesSinceUpdate;
    //TODO: Perlin

    //TODO: break data

    //TODO: add biome and more init stuff
    public Chunk(World world, IBlockProvider blockProvider,
            Vector3 worldPosition, int chunkPosX, int chunkPosZ, Biome biome) {
        this.ready = false;
        this.world = world;
        this.blockProvider = blockProvider;
        this.worldPosition = worldPosition;
        this.chunkPosX = chunkPosX;
        this.chunkPosZ = chunkPosZ;
        this.biome = biome;
        //perlin
        this.map = new byte[World.CHUNK_WIDTH * World.CHUNK_WIDTH * World.MAX_HEIGHT];
        this.lightmap = new byte[World.CHUNK_WIDTH * World.CHUNK_WIDTH * World.MAX_HEIGHT];
        this.heightmap = new boolean[World.CHUNK_WIDTH * World.CHUNK_WIDTH * World.MAX_HEIGHT];
        this.generationHeightmap = new int[World.CHUNK_WIDTH][World.CHUNK_WIDTH];
        Arrays.fill(lightmap, DARKNESS);
        Arrays.fill(heightmap, false);
        for (int[] arr : generationHeightmap) {
            Arrays.fill(arr, 47);
        }

        //prepare VoxelMeshes for the chunk
        //Chunks are further subdivided into 16x16x16 meshes
        for (int i = 0; i < World.MAX_HEIGHT / 16; i++) {
            meshes.add(new VoxelMesh());
            alphaMeshes.add(new VoxelMesh());
        }

        //breakstate init

        //random generation init
        executorService.submit(() -> {
            try {
                this.recalculating = true;
                this.fullRebuildOfLight = true;
                calculateChunk(worldPosition, blockProvider);
                updateLight();
                world.notifyNeighborsAboutLightChange(chunkPosX, chunkPosZ, false);
                resetMesh();
                ready = true;
                recalculateMesh();
            } catch (Exception e) {
                System.err.println("Error occurred while initializing chunk");
                e.printStackTrace();
            }
        });
    }

    //TODO: chunk methods

    private void fillSunlight() {
        for (int x = 0; x < World.CHUNK_WIDTH; x++) {
            for (int z = 0; z < World.CHUNK_WIDTH; z++) {
                for (int y = World.MAX_HEIGHT - 1; y > 0; y--) {
                    byte block = getByte(x, y, z);
                    if (block == 0) {
                        setLight(x, y, z, LIGHT);
                        heightmap[getLocationInArray(x, y, z)] = true;
                    } else {
                        break;
                    }
                }
            }
        }
    }

    //TODO: use real generation
    protected void calculateChunk(Vector3 worldPosition, IBlockProvider blockProvider) {
        //recalculate heightmap from perlin noise
        Vector3 worldPosOfXYZ = new Vector3();
        for (int y = 0; y < World.MAX_HEIGHT; y++) {
            for (int x = 0; x < World.CHUNK_WIDTH; x++) {
                for (int z = 0; z < World.CHUNK_WIDTH; z++) {
                    worldPosOfXYZ.set(x, y, z).add(worldPosition);
                    setBlock(x, y, z,
                            blockProvider.getBlockById(getByteAtWorldPosition(x, y, z, worldPosOfXYZ
                            )), false
                    );
                }
            }
        }
        //cave pass
        //final pass
    }

    //TODO: create tree

    protected byte getByteAtWorldPosition(int x, int y, int z, Vector3 worldPosition) {
        int height = generationHeightmap[x][z];
        //subsurface area, no noise impact
        if (y < 30) {
            return BlockProvider.stone.getId();
        }
        int surfaceY = y - 30;
        if (surfaceY > height) {
            return BlockProvider.air.getId();
        }
        //more calculations
        return BlockProvider.stone.getId();
    }

    //TODO: cavePass

    //TODO: finalPass

    public void setBlock(int x, int y, int z, Block block, boolean updateLight) {
        if (outsideThisChunkBounds(x, z)) {
            int xToFind = (int) Math.floor(chunkPosX + (x / 16f));
            int zToFind = (int) Math.floor(chunkPosZ + (z / 16f));

            Chunk chunk = world.findChunk(xToFind, zToFind);
            if (chunk == this) {
                System.out.println("Found myself");
            }
            if (chunk != null) {
                int normalizedX = x & 15;
                int normalizedZ = z & 15;
                chunk.setBlock(normalizedX, y, normalizedZ, block, updateLight);
                return;
            }
        }
        if (recalculating && ready) {
            return;
        }
        map[getLocationInArray(x, y, z)] = block.getId();
        blockCounter++;

        if (updateLight) {
//            resetLight(true);
        }
        needLightUpdate = true;
        resetMesh();
    }

    public byte getBlock(int x, int y, int z) {
        if (outsideHeightBounds(y)) {
            return 0;
        }
        return map[getLocationInArray(x & 15, y, z & 15)];
    }

    private int getLocationInArray(int x, int y, int z) {
        return (x * World.CHUNK_WIDTH + z) + (y * World.CHUNK_WIDTH * World.CHUNK_WIDTH);
    }

    public void recalculateMesh() {
        if (!ready) {
            return;
        }
        needMeshUpdate = false;
        recalculating = true;
        Set<VoxelMesh> toRebuild = new HashSet<>();
        Set<VoxelMesh> toRebuildAlpha = new HashSet<>();
        for (int y = 0; y < World.MAX_HEIGHT; y++) {
            VoxelMesh mesh = meshes.get((int) Math.floor(y / 16f));
            VoxelMesh alphaMesh = alphaMeshes.get((int) Math.floor(y / 16f));

            for (int x = 0; x < World.CHUNK_WIDTH; x++) {
                for (int z = 0; z < World.CHUNK_WIDTH; z++) {
                    byte block = map[getLocationInArray(x, y, z)];
                    if (block == 0) continue;
                    Block blockById = blockProvider.getBlockById(block);
                    if (blockById.getOpacity() > 31) {
                        mesh.addBlock(worldPosition, x, y, z, blockProvider, this, blockById);
                        toRebuild.add(mesh);
                    } else {
                        alphaMesh.addBlock(worldPosition, x, y, z, blockProvider, this, blockById);
                        toRebuildAlpha.add(alphaMesh);
                    }
                }
            }
        }
        //handle break state

        synchronized (syncToken) {
            for (BoxMesh mesh : toRebuild) {
                mesh.setNeedsRebuild();
            }
            for (BoxMesh mesh : toRebuildAlpha) {
                mesh.setNeedsRebuild();
            }
        }
        recalculating = false;
    }

    private boolean updateLight() {
        boolean lightUpdated = false;
        boolean lightUpdatedInLoop = true;
        boolean borderUpdated = false;
        if (fullRebuildOfLight) {
            Arrays.fill(lightmap, DARKNESS);
            Arrays.fill(heightmap, false);
            fillSunlight();
        }
        while (lightUpdatedInLoop) {
            lightUpdatedInLoop = false;
            for (int y = 0; y < World.MAX_HEIGHT; y++) {
                for (int x = 0; x < World.CHUNK_WIDTH; x++) {
                    for (int z = 0; z < World.CHUNK_WIDTH; z++) {
                        byte calculatedLight = calculatedLight(x, y, z);
                        byte currentLight = getBlockLight(x, y, z);
                        if (calculatedLight != currentLight) {
                            if (setLight(x, y, z, calculatedLight)) {
                                lightUpdated = true;
                                lightUpdatedInLoop = true;
                                if (x == 0 || x == World.CHUNK_WIDTH - 1 || z == 0 ||
                                        z == World.CHUNK_WIDTH - 1) {
                                    borderUpdated = true;
                                }
                            }
                        }
                    }
                }
            }
        }
        needLightUpdate = lightUpdated;
        fullRebuildOfLight = false;
        if (borderUpdated) {
            world.notifyNeighborsAboutLightChange(chunkPosX, chunkPosZ, false);
        }
        return !needLightUpdate;
    }

    private byte calculatedLight(int x, int y, int z) {
        //basic checks
        if (y == 0) {
            return DARKNESS;
        }
        if (heightmap[getLocationInArray(x, y, z)]) {
            return LIGHT;
        }

        byte block = map[getLocationInArray(x, y, z)];
        if (block != 0 && blockProvider.getBlockById(block).isLightSource()) {
            return LIGHT;
        } else {
            int opaqueValue = 3;
            if (block != 0 && !blockProvider.getBlockById(block).isLightSource()) {
                opaqueValue = blockProvider.getBlockById(block).getOpacity();
            }

            byte lightFront = getBlockLight(x + 1, y, z);
            byte lightBack = getBlockLight(x - 1, y, z);
            byte lightAbove = getBlockLight(x, y + 1, z);
            byte lightBelow = getBlockLight(x, y - 1, z);
            byte lightLeft = getBlockLight(x, y, z + 1);
            byte lightRight = getBlockLight(x, y, z - 1);

            byte finalLight = DARKNESS;

            if (lightFront < finalLight) {
                finalLight = (byte) (lightFront + opaqueValue);
            }
            if (lightBack < finalLight) {
                finalLight = (byte) (lightBack + opaqueValue);
            }
            if (lightAbove < finalLight) {
                finalLight = (byte) (lightAbove + opaqueValue);
            }
            if (lightBelow < finalLight) {
                finalLight = (byte) (lightBelow + opaqueValue);
            }
            if (lightLeft < finalLight) {
                finalLight = (byte) (lightLeft + opaqueValue);
            }
            if (lightRight < finalLight) {
                finalLight = (byte) (lightRight + opaqueValue);
            }

            if (finalLight > DARKNESS) {
                finalLight = DARKNESS;
            }
            if (finalLight < LIGHT) {
                finalLight = LIGHT;
            }
            return finalLight;
        }
    }

    private boolean setLight(int x, int y, int z, byte light) {
        int existingLight = lightmap[getLocationInArray(x, y, z)];
        if (existingLight != light) {
            lightmap[getLocationInArray(x, y, z)] = light;
            return true;
        }
        return false;
    }

    public boolean isBlockTransparent(int x, int y, int z, Block sourceBlock) {
        if (y < 0) {
            return false;
        }

        byte b = getBlock(x, y, z);
        Block blockbyId = blockProvider.getBlockById(b);
        if (sourceBlock != null && sourceBlock.getId() == b) {
            return false;
        }
        if (blockbyId.getOpacity() < 32) {
            return true;
        }
        switch (b) {
            case 0:
                return true;
            default:
                return false;
        }
    }

    public byte getByte(int x, int y, int z) {
        if (outsideHeightBounds(y)) {
            return 0;
        }
        if (outsideThisChunkBounds(x, z)) {
            Vector3 worldPosition = this.worldPosition.cpy().add(x, y, z);
            Chunk chunk = world.findChunk((int) Math.floor(worldPosition.x / World.CHUNK_WIDTH),
                    (int) Math.floor(worldPosition.z / World.CHUNK_WIDTH)
            );
            if (chunk != null && chunk.ready) {
                int normalizedX = x & 15;
                int normalizedZ = z & 15;
                return chunk.getBlock(normalizedX, y, normalizedZ);
            }
            return 0;
        }
        return map[getLocationInArray(x, y, z)];
    }

    private boolean outsideThisChunkBounds(int x, int z) {
        return x < 0 || z < 0 || x >= World.CHUNK_WIDTH || z >= World.CHUNK_WIDTH;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
    }

    public void resetMesh() {
        needMeshUpdate = true;
        if (ready) {
//            world.postChunkPriorityUpdate(this);
        }
    }

    //TODO: get2dNoise

    //TODO: get3DNoise

    private boolean outsideHeightBounds(int y) {
        return y < 0 || y >= World.MAX_HEIGHT;
    }

    public Vector3 getWorldPosition() {
        return worldPosition;
    }

    public Array<VoxelMesh> getMeshes() {
        return meshes;
    }

    public Array<VoxelMesh> getAlphaMeshes() {
        return alphaMeshes;
    }

    public boolean isRecalculating() {
        return false;
    }

    public int getBlockCounter() {
        return blockCounter;
    }

    public boolean isNeedLightUpdate() {
        return needLightUpdate;
    }

    public void resetLight(boolean force) {
        if (force) {
            fullRebuildOfLight = true;
        }
        needLightUpdate = true;
    }

    public byte getBlockLight(int x, int y, int z) {
        if (outsideHeightBounds(y)) {
            return DARKNESS;
        }
        try {
            if (outsideThisChunkBounds(x, z)) {
                int xToFind = (int) Math.floor(chunkPosX + (x / 16f));
                int zToFind = (int) Math.floor(chunkPosZ + (z / 16f));

                Chunk chunk = world.findChunk(xToFind, zToFind);
                if (chunk == this) {
                    System.out.println("Found myself");
                }
                if (chunk != null) {
                    int normalizedX = x & 15;
                    int normalizedZ = z & 15;
                    return chunk.getBlockLight(normalizedX, y, normalizedZ);
                } else {
                    return DARKNESS;
                }
            }
            if (heightmap[getLocationInArray(x, y, z)]) {
                return LIGHT;
            }
            return lightmap[getLocationInArray(x, y, z)];
        } catch (Exception ex) {
            System.out.println("Out of bounds (" + x + ", " + y + ", " + z + ")");
            return DARKNESS;
        }
    }

    public void update() {
        timesSinceUpdate++;
        if (timesSinceUpdate > 100) {
            timesSinceUpdate = 0;
            needLightUpdate = true;
        }
        if (!needLightUpdate && !needMeshUpdate) {
            return;
        }
        executorService.submit(() -> {
            if (needLightUpdate) {
                timesSinceUpdate = 0;
                boolean b = updateLight();
                if (b) {
                    resetMesh();
                }
            }
            if (needMeshUpdate) {
                recalculateMesh();
            }
        });
    }

    public void tick() {
    }
}
