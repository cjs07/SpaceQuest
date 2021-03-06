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
import com.flowpowered.noise.module.source.Perlin;

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

    private static double offsetIncrement = 0.005;

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

    private Array<VoxelMesh> meshes = new Array<>();
    private Array<VoxelMesh> alphaMeshes = new Array<>();
    private World world;
    private boolean isReady;
    //contains lighting data for each block
    private byte[] lightmap;
    //true if the block at a given position can see the sky (and thus is the highest block in the column for generation)
    private boolean[] heightmap;
    private int[][] generationHeightmap;
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
    private Perlin perlin;

    private int breakX;
    private int breakY;
    private int breakZ;
    private int breakState;

    public Chunk(World world, IBlockProvider blockProvider, Vector3 worldPosition, int chunkPosX, int chunkPosZ, Biome biome) {
        isReady = false;
        this.world = world;
        this.blockProvider = blockProvider;
        this.worldPosition = worldPosition;
        this.chunkPosX = chunkPosX;
        this.chunkPosZ = chunkPosZ;
        this.biome = biome;
        this.perlin = new Perlin();
        perlin.setSeed((int) world.getSeed());
        perlin.setFrequency(biome.perlinFrequency());
        perlin.setLacunarity(biome.perlinLacunarity());
        perlin.setOctaveCount(biome.perlinOctaves());
        perlin.setPersistence(biome.perlinPersistence());
        map = new byte[World.CHUNK_WIDTH * World.CHUNK_WIDTH * World.MAX_HEIGHT];
        lightmap = new byte[World.CHUNK_WIDTH * World.CHUNK_WIDTH * World.MAX_HEIGHT];
        heightmap = new boolean[World.CHUNK_WIDTH * World.CHUNK_WIDTH * World.MAX_HEIGHT];
        generationHeightmap = new int[World.CHUNK_WIDTH][World.CHUNK_WIDTH];
        Arrays.fill(lightmap, DARKNESS);
        Arrays.fill(heightmap, false);
        for (int[] arr : generationHeightmap) {
            Arrays.fill(arr, 47);
        }

        //prepare VoxelMeshes for the chunk. Each mesh is 16x16x16
        for (int i = 0; i < World.MAX_HEIGHT / 16; i++) {
            meshes.add(new VoxelMesh());
        }
        for (int i = 0; i < World.MAX_HEIGHT / 16; i++) {
            alphaMeshes.add(new VoxelMesh());
        }

        this.breakState = -1; //no existing breakstate

        if (random == null) {
            random = new Random();
            if (world.getSeed() != 0) {
                random.setSeed(world.getSeed());
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
                world.notifyNeighborsAboutLightChange(chunkPosX, chunkPosZ, false);
                isReady = true;
                recalculateMesh();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

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

    protected void calculateChunk(Vector3 worldPosition, Biome biome, IBlockProvider blockProvider) {
        //calculates a heightmap for the chunk
        double xOff = chunkPosX * 16 * offsetIncrement;
        double zOff = chunkPosZ * 16 * offsetIncrement;
        double startZ = zOff;
        for (int x = 0; x < World.CHUNK_WIDTH; x++) {
            for (int z = World.CHUNK_WIDTH - 1; z >= 0; z--) {
                generationHeightmap[x][z] += (biome.getHeight() * perlin.getValue(xOff, zOff, 0));
                zOff -= offsetIncrement;
            }
            xOff += offsetIncrement;
            zOff = startZ;
        }

        //fills the chunk blocks
        Vector3 worldPosOfXYZ = new Vector3();
        for (int y = 0; y < World.MAX_HEIGHT; y++) {
            for (int x = 0; x < World.CHUNK_WIDTH; x++) {
                for (int z = 0; z < World.CHUNK_WIDTH; z++) {
                    worldPosOfXYZ.set(x, y, z).add(worldPosition);
                    setBlock(x, y, z, blockProvider.getBlockById(getByteAtWorldPosition(x, y, z, biome, worldPosOfXYZ)), false);
                }
            }
        }
        cavePass();
        finalPass();
//        for (int y = 0; y < World.MAX_HEIGHT; y++) {
//            for (int x = 0; x < World.CHUNK_WIDTH; x++) {
//                for (int z = 0; z < World.CHUNK_WIDTH; z++) {
//                    float v = random.nextFloat();
//                    if (getBlock(x, y, z) == BlockProvider.grass.getId() && getBlock(x, y + 1, z) == 0 && getBlock(x, y + 2, z) == 0) {
//                        if (v < 0.2) {
//                            setBlock(x, y + 1, z, BlockProvider.straws, false);
//                            continue;
//                        }
//
//                        if (v < 0.3) {
//                            setBlock(x, y + 1, z, BlockProvider.flower, false);
//                            continue;
//                        }
//                    }
//                }
//            }
//        }
//
//        for (int y = 0; y < World.MAX_HEIGHT - 12; y++) {
//            for (int x = 4; x < World.CHUNK_WIDTH - 4; x++) {
//                for (int z = 4; z < World.CHUNK_WIDTH - 4; z++) {
//                    byte block = getBlock(x, y, z);
//                    if ((block == BlockProvider.grass.getId() || block == BlockProvider.straws.getId()) && getBlock(x, y + 1, z) == 0) {
//                        float v = random.nextFloat();
//                        if (v < 0.009) {
//                            createTree(x, y, z);
//                        }
//                    }
//                }
//            }
//        }
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
        int height = generationHeightmap[x][z];
        //subsurface area, noise does not affect this height
        if (y < 30) {
            return BlockProvider.stone.getId();
        }
//            if (y > 30 && y < 34) {
//                return biome.getGroundFillerBlock();
//            }
//            if (y == 34) {
//                return biome.getSurfaceBlock();
//            }

        int surfaceY = y - 30;
        if (surfaceY > height) {
            return BlockProvider.air.getId();
        }
        if (surfaceY == height) {
            return biome.getSurfaceBlock();
        }
        if (height - surfaceY < 4) {
            return biome.getGroundFillerBlock();
        }
        return BlockProvider.stone.getId();
    }

    private void cavePass() {
        for (int x = 0; x < World.CHUNK_WIDTH; x++) {
            for (int y = 0; y < World.MAX_HEIGHT; y++) {
                for (int z = 0; z < World.CHUNK_WIDTH; z++) {
                    double caveValue = Math.abs(perlin.getValue(x * offsetIncrement, y * offsetIncrement, z * offsetIncrement));
                    int worldX = World.CHUNK_WIDTH * chunkPosX;
                    int worldZ = World.CHUNK_WIDTH * chunkPosZ;
                    double caveDensity = SimplexNoise.noise((worldX + x) * 0.01f, y * 0.02f, (worldZ + z) * 0.01f);
                    double caveDensity2 = SimplexNoise2.noise((worldX + x) * 0.01f, y * 0.02f, (worldZ + z) * 0.01f);
                    if (caveDensity > 0.45 && caveDensity < 0.70 && caveDensity2 > 0.45 && caveDensity2 < 0.70) {
                        setBlock(x, y, z, BlockProvider.air, false);
                    }
//                    if (caveValue < biome.getCaveDensity()) {
//                        setBlock(x, y, z, BlockProvider.air, false);
//                    }
                }
            }
        }
    }

    /**
     * The final pass in chunk generation. Primarily decorative, replaces dirt with grass where upper layers were
     * removed, and adds decorative touches (such as rotating logs, and adding other block variants
     */
    private void finalPass() {
        for (int x = 0; x < World.CHUNK_WIDTH; x++) {
            for (int y = 0; y < World.MAX_HEIGHT; y++) {
                for (int z = 0; z < World.CHUNK_WIDTH; z++) {
                    if (blockProvider.getBlockById(getBlock(x, y, z)) == BlockProvider.dirt) {
                        if (blockProvider.getBlockById(getBlock(x, y + 1, z)) == BlockProvider.air) {
                            setBlock(x, y, z, BlockProvider.grass, false);
                        }
                    }
                }
            }
        }
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

    public void setBreakState(int x, int y, int z, int state) {
        if (outsideThisChunkBounds(x, z)) {
            System.out.println("this is strange");
            int xToFind = (int) Math.floor(chunkPosX + (x / 16f));
            int zToFind = (int) Math.floor(chunkPosZ + (z / 16f));

            Chunk chunk = world.findChunk(xToFind, zToFind);
            if (chunk == this) {
                System.out.println("Found myself, how silly, to make such a simple math error");
            }
            if (chunk != null) {
                int normalizedX = x & 15;
                int normalizedZ = z & 15;
                chunk.setBreakState(normalizedX, y, normalizedZ, state);
                return;
            }
        }
        System.out.println("setting break at " + x + ", " + y + ", " + z);
        if (isRecalculating && isReady) {
            return;
        }
        if (x != breakX || y != breakY || z != breakZ || state != breakState) {
            breakX = x;
            breakY = y;
            breakZ = z;
            breakState = state;
            resetMesh();
        }
    }

    public void setBlock(int x, int y, int z, Block block, boolean updateLight) {
        if (outsideThisChunkBounds(x, z)) {
            int xToFind = (int) Math.floor(chunkPosX + (x / 16f));
            int zToFind = (int) Math.floor(chunkPosZ + (z / 16f));

            Chunk chunk = world.findChunk(xToFind, zToFind);
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
        return (x * World.CHUNK_WIDTH + z) + (y * World.CHUNK_WIDTH * World.CHUNK_WIDTH);
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
        for (int y = 0; y < World.MAX_HEIGHT; y++) {
            VoxelMesh voxelMesh = meshes.get((int) Math.floor(y / 16));
            VoxelMesh alphaVoxelMesh = alphaMeshes.get((int) Math.floor(y / 16));

            for (int x = 0; x < World.CHUNK_WIDTH; x++) {
                for (int z = 0; z < World.CHUNK_WIDTH; z++) {
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
        if (breakState != -1) {
            VoxelMesh mesh = meshes.get((int) Math.floor(breakY / 16));
            mesh.addBlock(worldPosition, breakX, breakY, breakZ, blockProvider, this,
                    blockProvider.getBlockById(getBlock(breakX, breakY, breakZ)), breakState);
            toRebuild.add(mesh);
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
            for (int y = 0; y < World.MAX_HEIGHT; y++) {
                for (int x = 0; x < World.CHUNK_WIDTH; x++) {
                    for (int z = 0; z < World.CHUNK_WIDTH; z++) {
                        byte calculatedLight = calculatedLight(x, y, z);
                        byte currentLight = getBlockLight(x, y, z);

                        if (calculatedLight != currentLight) {
                            boolean b = setLight(x, y, z, calculatedLight);
                            if (b) {
                                lightUpdated = true;
                                lightUpdatedInLoop = true;
                                if (z == 0 || z == World.CHUNK_WIDTH - 1 || x == 0 || x == World.CHUNK_WIDTH - 1) {
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
            Chunk chunk = world.findChunk((int) Math.floor(worldPosition.x / World.CHUNK_WIDTH), (int) Math.floor(worldPosition.z / World.CHUNK_WIDTH));
            if (chunk != null && chunk.isReady) {
                int normalizedX = x & 15;
                int normalizedZ = z & 15;
                return chunk.getBlock(normalizedX, y, normalizedZ);
            }
//            Biome biome = world.findBiome((int) Math.floor(worldPosition.x / World.CHUNK_WIDTH), (int) Math.floor(worldPosition.z / World.CHUNK_WIDTH));
//            return getByteAtWorldPosition(x, y, z, biome, worldPosition);
            return 0;
            // }
        }
        return map[getLocationInArray(x, y, z)];
    }

    private boolean outsideThisChunkBounds(int x, int z) {
        return x < 0 || z < 0 || x >= World.CHUNK_WIDTH || z >= World.CHUNK_WIDTH;
    }

    private boolean outsideHeightBounds(int y) {
        return y < 0 || y >= World.MAX_HEIGHT;
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

                Chunk chunk = world.findChunk(xToFind, zToFind);
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
            world.postChunkPriorityUpdate(this);
        }
    }

    public boolean isRecalculating() {
        return isRecalculating;
    }

    public boolean isNeedMeshUpdate() {
        return needMeshUpdate;
    }
}
