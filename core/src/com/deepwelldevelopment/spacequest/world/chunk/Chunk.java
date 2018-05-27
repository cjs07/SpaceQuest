package com.deepwelldevelopment.spacequest.world.chunk;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.deepwelldevelopment.spacequest.block.Block;
import com.deepwelldevelopment.spacequest.block.BlockProvider;
import com.deepwelldevelopment.spacequest.block.IBlockProvider;
import com.deepwelldevelopment.spacequest.client.render.BoxMesh;
import com.deepwelldevelopment.spacequest.client.render.VoxelMesh;
import com.deepwelldevelopment.spacequest.util.PositionUtils;
import com.deepwelldevelopment.spacequest.world.World;
import com.deepwelldevelopment.spacequest.world.biome.Biome;
import com.deepwelldevelopment.spacequest.world.noise.SimplexNoise;
import com.deepwelldevelopment.spacequest.world.noise.SimplexNoise2;
import com.deepwelldevelopment.spacequest.world.noise.SimplexNoise3;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Represents a subset of the game world. A chunk is a 16x256x16 section of blocks. A chunk technically expands from
 * y=0 to the world height limit
 */
public class Chunk {

    public static final byte LIGHT = 1; //1 is brightest light
    public static final byte DARKNESS = 32; //32 is darkest

    private static final ExecutorService executorService = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors() - 1);

    protected static Random random;
    protected static Vector3 landscapeRandomOffset1;
    protected static Vector3 landscapeRandomOffset2;
    protected static Vector3 landscapeRandomOffset3;
    protected static Vector3 landscapeRandomOffset4;
    protected static Vector3 landscapeRandomOffset5;
    //contains block id for each block in the chunk
    //will be changed eventually to use a State model along with a statically typed block system so blocks can be
    // compared using double equals
    private final byte[] map;
    private final IBlockProvider blockProvider;
    private final Vector3 worldPosition;
    private final int chunkPosX;
    private final int chunkPosZ;
    private final Object syncToken = new Object();

    protected Array<VoxelMesh> meshes = new Array<>();
    protected Array<VoxelMesh> alphaMeshes = new Array<>();

    private boolean isReady;

    //contains lighting data for each block
    private byte[] lightmap;
    //true if the block at a given position can see the sky (and thus is the highest block in the column for generation)
    private boolean[] heightmap;
    private Biome biome;
    private int blockCounter;
    private boolean active = true;
    private boolean isRecalculating;
    private ArrayMap<Long, Double> noiseCache2d = new ArrayMap<>();
    private ArrayMap<Long, Double> noiseCache3d = new ArrayMap<>();
    private boolean needLightUpdate = true;
    private boolean fullRebuildOfLight;
    private boolean needMeshUpdate;
    private int timesSinceUpdate;

    public Chunk(IBlockProvider blockProvider, Vector3 worldPosition, int chunkPosX, int chunkPosZ, Biome biome) {
        isReady = false;
        this.blockProvider = blockProvider;
        this.worldPosition = worldPosition;
        this.chunkPosX = chunkPosX;
        this.chunkPosZ = chunkPosZ;
        this.biome = biome;
        map = new byte[World.WIDTH * World.WIDTH * World.HEIGHT];
        lightmap = new byte[World.WIDTH * World.WIDTH * World.HEIGHT];
        heightmap = new boolean[World.WIDTH * World.WIDTH * World.HEIGHT];
        Arrays.fill(lightmap, DARKNESS);
        Arrays.fill(heightmap, false);

        //prepare VoxelMeshes for the chunk. Each mesh is 16x16x16
        for (int i = 0; i < World.HEIGHT / 16; i++) {
            meshes.add(new VoxelMesh());
        }
        for (int i = 0; i < World.HEIGHT / 16; i++) {
            alphaMeshes.add(new VoxelMesh());
        }

        if (random == null) {
            random = new Random();
            if (World.SEED != 0) {
                random.setSeed(World.SEED);
            }
            landscapeRandomOffset1 = new Vector3((float) random.nextDouble() * 10000,
                    (float) random.nextDouble() * 10000, (float) random.nextDouble() * 10000);
            landscapeRandomOffset2 = new Vector3((float) random.nextDouble() * 10000,
                    (float) random.nextDouble() * 10000, (float) random.nextDouble() * 10000);
            landscapeRandomOffset3 = new Vector3((float) random.nextDouble() * 10000,
                    (float) random.nextDouble() * 10000, (float) random.nextDouble() * 10000);
            landscapeRandomOffset4 = new Vector3((float) random.nextDouble() * 10000,
                    (float) random.nextDouble() * 10000, (float) random.nextDouble() * 10000);
            landscapeRandomOffset5 = new Vector3((float) random.nextDouble() * 10000,
                    (float) random.nextDouble() * 10000, (float) random.nextDouble() * 10000);
        }
        executorService.submit(() -> {
            try {
                isRecalculating = true;
                fullRebuildOfLight = true;
                calculateChunk(worldPosition, biome, blockProvider);
                updateLight();
                resetMesh();
                World.notifyNeighborsAboutLightChange(chunkPosX, chunkPosZ, false);
                isReady = true;
                recalculateMesh();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void fillSunlight() {
        for (int x = 0; x < World.WIDTH; x++) {
            for (int z = 0; z < World.WIDTH; z++) {
                for (int y = World.HEIGHT - 1; y > 0; y--) {
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

    protected void calculateChunk(Vector3 worldPosition, Biome biome, IBlockProvider blockProvider) {
        Vector3 worldPosOfXYZ = new Vector3();
        for (int y = 0; y < World.HEIGHT; y++) {
            for (int x = 0; x < World.WIDTH; x++) {
                for (int z = 0; z < World.WIDTH; z++) {
                    worldPosOfXYZ.set(x, y, z).add(worldPosition);
                    setBlock(x, y, z, blockProvider.getBlockById(getByteAtWorldPosition(x, y, z, biome, worldPosOfXYZ)), false);
                }
            }
        }
        for (int y = 0; y < World.HEIGHT; y++) {
            for (int x = 0; x < World.WIDTH; x++) {
                for (int z = 0; z < World.WIDTH; z++) {
                    float v = random.nextFloat();
                    if (getBlock(x, y, z) == BlockProvider.grass.getId() && getBlock(x, y + 1, z) == 0 && getBlock(x, y + 2, z) == 0) {
                        if (v < 0.2) {
                            setBlock(x, y + 1, z, BlockProvider.straws, false);
                            continue;
                        }

                        if (v < 0.3) {
                            setBlock(x, y + 1, z, BlockProvider.flower, false);
                            continue;
                        }
                    }
                }
            }
        }

        for (int y = 0; y < World.HEIGHT - 12; y++) {
            for (int x = 4; x < World.WIDTH - 4; x++) {
                for (int z = 4; z < World.WIDTH - 4; z++) {
                    byte block = getBlock(x, y, z);
                    if ((block == BlockProvider.grass.getId() || block == BlockProvider.straws.getId()) && getBlock(x, y + 1, z) == 0) {
                        float v = random.nextFloat();
                        if (v < 0.009) {
                            createTree(x, y, z);
                        }
                    }
                }
            }
        }
    }

    public void createTree(int rootX, int rootY, int rootZ) {
        try {
            int treeHight = 11;

            for (int treeY = 0; treeY < treeHight; treeY++) {
                setBlock(rootX, treeY + rootY, rootZ, BlockProvider.treeTrunk, false);
            }

            int bareTrunkY = 0;
            while (bareTrunkY < 7 + (treeHight / 10)) {
                bareTrunkY = random.nextInt(12);
            }

            int radius = 4;
            for (int treeY = 0; treeY < treeHight; treeY++) {
                for (int xc = -radius; xc <= radius; ++xc) {
                    for (int zc = -radius; zc <= radius; ++zc) {
                        if (xc * xc + zc * zc <= radius * radius) {
                            if (random.nextInt(1000) > 100) {
                                setBlock(xc + rootX, treeY + (rootY + bareTrunkY), zc + rootZ, BlockProvider.leaves, false);
                            }
                        }
                    }
                }
                radius--;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    protected byte getByteAtWorldPosition(int x, int y, int z, Biome biome, Vector3 worldPosition) {
        if (y == 1) {
            return BlockProvider.limeStone.getId();
        }

        if (y == World.HEIGHT - 1 || y == World.HEIGHT) {
            return 0;
        }

//        if (y == random.nextInt(80) && random.nextFloat() < 0.19f && !isBlockTransparent(x, y - 1, z) &&
//                (getByteAtWorldPosition(x, y - 1, z, biome, worldPosition) == BlockProvider.grass.getId())) {
//            return BlockProvider.light.getId();
//        }


//        double caveDensity = SimplexNoise.noise(worldPosition.x * 0.01f, worldPosition.y * 0.02f, worldPosition.z * 0.01f);
//        double caveDensity2 = SimplexNoise2.noise(worldPosition.x * 0.01f, worldPosition.y * 0.02f, worldPosition.z * 0.01f);
//        if (caveDensity > 0.45 && caveDensity < 0.70 && caveDensity2 > 0.45 && caveDensity2 < 0.70) {
//            return 0;
//        }

        double baseDensity = 0;

        {
            float frequency = 0.001f;
            float weight = 1.0f - (y / World.HEIGHT);
            float weight2 = 0.1f - (y / World.HEIGHT);

            for (int i = 0; i < 3; i++) {
                baseDensity += SimplexNoise.noise(worldPosition.x * frequency, worldPosition.y * frequency, worldPosition.z * frequency) * weight;
                baseDensity += SimplexNoise2.noise(worldPosition.x * frequency, worldPosition.y * frequency * 2, worldPosition.z * frequency) * weight2;
                frequency *= 3.5f;
                weight *= 0.2f;
            }
        }

        double mountainDensity = 0;

        {
            float frequency = 0.001f;
            float weight = 1f;

            for (int i = 0; i < 3; i++) {
                mountainDensity += SimplexNoise.noise(worldPosition.x * frequency, worldPosition.y * frequency * 2, worldPosition.z * frequency) * weight;
                mountainDensity += SimplexNoise3.noise(worldPosition.x * frequency, worldPosition.y * frequency, worldPosition.z * frequency) * weight;
                frequency *= 4.3f + (y / World.HEIGHT);
                weight *= 0.4f;
            }
        }
        mountainDensity += 1;

        mountainDensity *= 48;

        if (mountainDensity > y) {
            double sandStoneNoise = SimplexNoise.noise(worldPosition.x * 0.008, worldPosition.y * 0.09, worldPosition.z * 0.006) * 0.019;
            double shaleNoise = SimplexNoise2.noise(worldPosition.x * 0.022, worldPosition.y * 0.1, worldPosition.z * 0.026) * 0.053;

            if (sandStoneNoise >= 0.0009) {
                return BlockProvider.sandStone.getId();
            } else if (shaleNoise >= 0.0001) {
                return BlockProvider.shale.getId();
            }

            return BlockProvider.limeStone.getId();
        }


        baseDensity += 1;

        baseDensity *= 48;


        if (baseDensity > y) {
            if (y > 48) {
                return BlockProvider.grass.getId();
            } else {
                double sandStoneNoise = SimplexNoise.noise(worldPosition.x * 0.008, worldPosition.y * 0.09, worldPosition.z * 0.006) * 0.019;
                double shaleNoise = SimplexNoise2.noise(worldPosition.x * 0.022, worldPosition.y * 0.1, worldPosition.z * 0.026) * 0.053;

                if (sandStoneNoise >= 0.0009) {
                    return BlockProvider.sandStone.getId();
                } else if (shaleNoise >= 0.0001) {
                    return BlockProvider.shale.getId();
                }

                return BlockProvider.limeStone.getId();
            }
        }

        if (y < 49 && y > 0) {
            return BlockProvider.water.getId();
        }

        return 0;
    }

    protected double get2dNoise(Vector3 pos, Vector3 offset, double scale) {
        long posHash = PositionUtils.hashOfPosition((int) (pos.x + offset.x), (int) (pos.z + offset.z));
        if (noiseCache2d.containsKey(posHash)) {
            return noiseCache2d.get(posHash);
        }

        double noiseX = (double) (pos.x + offset.x) * scale;
        double noiseZ = (double) (pos.z + offset.z) * scale;
        double noise = SimplexNoise.noise(noiseX, noiseZ);

        noiseCache2d.put(posHash, noise);
        return noise;
    }

    protected double get3dNoise(Vector3 pos, Vector3 offset, double scale) {
        long posHash = PositionUtils.hashOfPosition((int) (pos.x + offset.x), (int) (pos.z + offset.z));
        if (noiseCache3d.containsKey(posHash)) {
            return noiseCache3d.get(posHash);
        }

        double noiseX = (double) (pos.x + offset.x) * scale;
        double noiseY = (double) (pos.y + offset.y) * scale;
        double noiseZ = (double) (pos.z + offset.z) * scale;
        double noise = SimplexNoise.noise(noiseX, noiseY, noiseZ);

        noiseCache3d.put(posHash, noise);
        return noise;
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

    public void setBlock(int x, int y, int z, Block block, boolean updateLight) {
        if (outsideThisChunkBounds(x, z)) {
            int xToFind = (int) Math.floor(chunkPosX + (x / 16f));
            int zToFind = (int) Math.floor(chunkPosZ + (z / 16f));

            Chunk chunk = World.findChunk(xToFind, zToFind);
            if (chunk == this) {
                System.out.println("Found myself, how silly, to make such a simple math error");
            }
            if (chunk != null) {
                int normalizedX = x & 15;
                int normalizedZ = z & 15;
                chunk.setBlock(normalizedX, y, normalizedZ, block, updateLight);
                return;
            }
        }
        if (isRecalculating && isReady) {
            return;
        }
        map[getLocationInArray(x, y, z)] = block.getId();
        blockCounter++;

        if (updateLight) {
            resetLight(true);
        }
        needLightUpdate = true;
        resetMesh();
    }

    public byte getBlock(int x, int y, int z) {
        if (outsideHeightBounds(y)) {
            return 0;
        }

        int localX = x & 15;
        int localY = y;
        int localZ = z & 15;
        return map[getLocationInArray(localX, localY, localZ)];
    }

    private int getLocationInArray(int x, int y, int z) {
        return (x * World.WIDTH + z) + (y * World.WIDTH * World.WIDTH);
    }

    public int getBlockCounter() {
        return blockCounter;
    }

    // TODO: 5/22/2018 HANDLE REMOVED BLOCKS

    /**
     * Takes all blocks stored in the chunk and adds them to the VoxelMesh. Removed blocks are not handled yet
     */
    public void recalculateMesh() {
        if (!isReady) {
            return;
        }
        needMeshUpdate = false;
        isRecalculating = true;
        Set<VoxelMesh> toRebuild = new HashSet<>();
        Set<VoxelMesh> toRebuildAlpha = new HashSet<>();
        for (int y = 0; y < World.HEIGHT; y++) {
            VoxelMesh voxelMesh = meshes.get((int) Math.floor(y / 16));
            VoxelMesh alphaVoxelMesh = alphaMeshes.get((int) Math.floor(y / 16));

            for (int x = 0; x < World.WIDTH; x++) {
                for (int z = 0; z < World.WIDTH; z++) {
                    byte block = map[getLocationInArray(x, y, z)];
                    if (block == 0) continue;
                    Block blockById = blockProvider.getBlockById(block);
                    if (blockById.getOpacity() > 31) {
                        voxelMesh.addBlock(worldPosition, x, y, z, blockProvider, this, blockById);
                        toRebuild.add(voxelMesh);
                    } else {
                        alphaVoxelMesh.addBlock(worldPosition, x, y, z, blockProvider, this, blockById);
                        toRebuildAlpha.add(alphaVoxelMesh);
                    }
                }
            }
        }

        synchronized (syncToken) {
            for (BoxMesh voxelMesh : toRebuild) {
                voxelMesh.setNeedsRebuild();
            }
            for (BoxMesh voxelMesh : toRebuildAlpha) {
                voxelMesh.setNeedsRebuild();
            }
        }
        isRecalculating = false;
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
            for (int y = 0; y < World.HEIGHT; y++) {
                for (int x = 0; x < World.WIDTH; x++) {
                    for (int z = 0; z < World.WIDTH; z++) {
                        byte calculatedLight = calculatedLight(x, y, z);
                        byte currentLight = getBlockLight(x, y, z);

                        if (calculatedLight != currentLight) {
                            boolean b = setLight(x, y, z, calculatedLight);
                            if (b) {
                                lightUpdated = true;
                                lightUpdatedInLoop = true;
                                if (z == 0 || z == World.WIDTH - 1 || x == 0 || x == World.WIDTH - 1) {
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
            World.notifyNeighborsAboutLightChange(chunkPosX, chunkPosZ, false);
        }
        return !needLightUpdate;
    }

    private byte calculatedLight(int x, int y, int z) {
        //basic checks
        if (y == 0) { //below world mesh
            return DARKNESS;
        }
        if (heightmap[getLocationInArray(x, y, z)]) { //block can see the sky
            return LIGHT;
        }

        byte block = map[getLocationInArray(x, y, z)];
        if (block != 0 && blockProvider.getBlockById(block).isLightSource()) { //block is a light source
            return LIGHT;
        } else { //block is not a light source
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

        if ((existingLight != light)) {
            lightmap[getLocationInArray(x, y, z)] = light;
            return true;
        }
        return false;
    }

    /**
     * Check if the block at the specified position exists or not. This is used by the VoxelMesh
     * to determinate if it needs to draw a mesh face or not depending on if it will be visible
     * or hidden by another block.
     *
     * @param x
     * @param y
     * @param z
     * @return
     */
    public boolean isBlockTransparent(int x, int y, int z, Block sourceBlock) {
        if (y < 0) {
            return false; //Bottom should be false since we will never see it.
        }

        byte b = getByte(x, y, z);
        Block blockById = blockProvider.getBlockById(b);
        if (sourceBlock != null && sourceBlock.getId() == b) {
            return false;
        }
        if (blockById.getOpacity() < 32) {
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
            Chunk chunk = World.findChunk((int) Math.floor(worldPosition.x / World.WIDTH), (int) Math.floor(worldPosition.z / World.WIDTH));
            if (chunk != null && chunk.isReady) {
                int normalizedX = x & 15;
                int normalizedZ = z & 15;
                return chunk.getBlock(normalizedX, y, normalizedZ);
            }
            Biome biome = World.findBiome((int) Math.floor(worldPosition.x / World.WIDTH), (int) Math.floor(worldPosition.z / World.WIDTH));
            return getByteAtWorldPosition(x, y, z, biome, worldPosition);
            // }
        }
        return map[getLocationInArray(x, y, z)];
    }

    private boolean outsideThisChunkBounds(int x, int z) {
        return x < 0 || z < 0 || x >= World.WIDTH || z >= World.WIDTH;
    }

    private boolean outsideHeightBounds(int y) {
        return y < 0 || y >= World.HEIGHT;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
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

                Chunk chunk = World.findChunk(xToFind, zToFind);
                if (chunk == this) {
                    System.out.println("Found myself!");
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
            byte light = lightmap[getLocationInArray(x, y, z)];
            return light;
        } catch (Exception ex) {
            System.out.println("Out of bounds " + x + " " + y + " " + z);
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
                return;
            }
            if (needMeshUpdate) {
                recalculateMesh();
            }
        });
    }

    public void tick() {
    }

    public void render() {
    }

    public void resetMesh() {
        needMeshUpdate = true;
        if (isReady) {
            World.postChunkPriorityUpdate(this);
        }
    }

    public boolean isRecalculating() {
        return isRecalculating;
    }

    public boolean isNeedMeshUpdate() {
        return needMeshUpdate;
    }
}