package com.deepwelldevelopment.spacequest.world.chunk;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.deepwelldevelopment.spacequest.block.Block;
import com.deepwelldevelopment.spacequest.block.BlockProvider;
import com.deepwelldevelopment.spacequest.block.IBlockProvider;
import com.deepwelldevelopment.spacequest.client.render.BoxMesh;
import com.deepwelldevelopment.spacequest.client.render.VoxelMesh;
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

public class Chunk {

    public static final byte LIGHT = 1;
    public static final byte DARKNESS = 32;

    private static final ExecutorService executorService =
            Executors.newFixedThreadPool(
                    Runtime.getRuntime().availableProcessors() - 1);

    private static double offsetIncrement = 0.005;

    protected static Random random;
    protected static Vector3 landscapeRandomOffset1;
    protected static Vector3 landscapeRandomOffset2;
    protected static Vector3 landscapeRandomOffset3;
    protected static Vector3 landscapeRandomOffset4;
    protected static Vector3 landscapeRandomOffset5;

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
    private ArrayMap<Long, Double> noiseCache2d = new ArrayMap<>();
    private ArrayMap<Long, Double> noiseCache3d = new ArrayMap<>();
    private boolean needLightUpdate = true;
    private boolean fullRebuildOfLight;
    private boolean needMeshUpdate;
    private int timesSinceUpdate;
    private Perlin perlin;

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
        this.perlin = new Perlin();
        this.perlin.setSeed((int) world.getSeed());
        this.perlin.setFrequency(biome.perlinFrequency());
        this.perlin.setLacunarity(biome.perlinLacunarity());
        this.perlin.setOctaveCount(biome.perlinOctaves());
        this.perlin.setPersistence(biome.perlinPersistence());
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

        if (random == null) {
            random = new Random();
            if (world.getSeed() != 0) {
                random.setSeed(world.getSeed());
            }
            landscapeRandomOffset1 = new Vector3((float) random.nextDouble() * 10000,
                    (float) random.nextDouble() * 10000, (float) random.nextDouble() * 10000
            );
            landscapeRandomOffset2 = new Vector3((float) random.nextDouble() * 10000,
                    (float) random.nextDouble() * 10000, (float) random.nextDouble() * 10000
            );
            landscapeRandomOffset3 = new Vector3((float) random.nextDouble() * 10000,
                    (float) random.nextDouble() * 10000, (float) random.nextDouble() * 10000
            );
            landscapeRandomOffset4 = new Vector3((float) random.nextDouble() * 10000,
                    (float) random.nextDouble() * 10000, (float) random.nextDouble() * 10000
            );
            landscapeRandomOffset5 = new Vector3((float) random.nextDouble() * 10000,
                    (float) random.nextDouble() * 10000, (float) random.nextDouble() * 10000
            );
        }

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

    protected void calculateChunk(Vector3 worldPosition, IBlockProvider blockProvider) {
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
                    setBlock(x, y, z, blockProvider
                                    .getBlockById(getByteAtWorldPosition(x, y, z, worldPosOfXYZ)),
                            false
                    );
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
                    double caveValue = Math.abs(
                            perlin.getValue(x * offsetIncrement, y * offsetIncrement,
                                    z * offsetIncrement
                            ));
                    int worldX = World.CHUNK_WIDTH * chunkPosX;
                    int worldZ = World.CHUNK_WIDTH * chunkPosZ;
                    double caveDensity = SimplexNoise
                            .noise((worldX + x) * 0.01f, y * 0.02f, (worldZ + z) * 0.01f);
                    double caveDensity2 = SimplexNoise2
                            .noise((worldX + x) * 0.01f, y * 0.02f, (worldZ + z) * 0.01f);
                    if (caveDensity > 0.45 && caveDensity < 0.70 && caveDensity2 > 0.45 &&
                            caveDensity2 < 0.70) {
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
     * The final pass in chunk generation. Primarily decorative, replaces dirt with grass where
     * upper layers were removed, and adds decorative touches (such as rotating logs, and adding
     * other block variants
     */
    private void finalPass() {
        for (int x = 0; x < World.CHUNK_WIDTH; x++) {
            for (int y = 0; y < World.MAX_HEIGHT; y++) {
                for (int z = 0; z < World.CHUNK_WIDTH; z++) {
                    if (blockProvider.getBlockById(getBlock(x, y, z)) == BlockProvider.dirt) {
                        if (blockProvider.getBlockById(getBlock(x, y + 1, z)) ==
                                BlockProvider.air) {
                            setBlock(x, y, z, BlockProvider.grass, false);
                        }
                    }
                }
            }
        }
    }

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
            resetLight(true);
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

    public void setBreakState(int x, int y, int z, int state) {
    }
}
