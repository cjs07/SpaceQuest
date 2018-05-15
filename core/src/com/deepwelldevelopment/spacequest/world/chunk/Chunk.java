package com.deepwelldevelopment.spacequest.world.chunk;

import com.deepwelldevelopment.spacequest.block.Block;
import com.deepwelldevelopment.spacequest.world.World;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Represents a subset of the game world. A chunk is a 16x256x16 section of blocks. A chunk technically expands from
 * y=0 to the world height limit
 */
public class Chunk {

    private World world;

    /** The blocks contained in this chunk */
    private Block[][][] blocks;

    private int x;
    private int z;

    private int averageHeight = 16;

    /** The initial height condition for the chunk. Created during world generation */
    private int[][] heightmap;

    public Chunk(World world, int x, int z) {
        this.world = world;
        this.x = x;
        this.z = z;
        blocks = new Block[16][World.MAX_HEIGHT][16];
        heightmap = new int[16][16];
    }

    /***
     * Adjusts the heightmap of the chunk at a given (x, z) column
     * @param heightPercent
     * @param x
     * @param z
     */
    public void adjustHeight(double heightPercent, int x, int z) {
        heightmap[x][z] = averageHeight + (int) Math.floor(averageHeight * heightPercent);
    }

    /**
     * Generates the terrain of this chunk according to the heightmap
     */
    public void generate() {
        for (int x = 0; x < blocks.length; x++) {
            for (int z = 0; z < blocks[0][0].length; z++) {
                int height = heightmap[x][z];
                for (int y = 0; y < height; y++) {
                    blocks[x][y][z] = new Block("dirt", x, y, z);
                }
                blocks[x][height][z] = new Block("grass", x, height, z);
            }
        }
    }

    public boolean setBlock(int x, int y, int z, Block block) {
        if (y < 0 || y > World.MAX_HEIGHT) {
            return false;
        }
        blocks[x][y][z] = block;
        return true;
    }

    public Block getBlock(int x, int y, int z) {
        if (x < 0 || x >= blocks.length) {
            return null;
        }
        if (y < 0 || x >= blocks[0].length) {
            return null;
        }
        if (z < 0 || z >= blocks[0][0].length) {
            return null;
        }
        return blocks[x][y][z];
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    public Block[][][] getBlocks() {

        return blocks;
    }

    public ArrayList<Block> getBlockList() {
        ArrayList<Block> ret = new ArrayList<>();
        for (int x = 0; x < blocks.length; x++) {
            for (int y = 0; y < blocks[0].length; y++) {
                ret.addAll(Arrays.asList(blocks[x][y]).subList(0, blocks[0][0].length));
            }
        }
        return ret;
    }
}
