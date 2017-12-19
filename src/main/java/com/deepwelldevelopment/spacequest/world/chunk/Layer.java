package com.deepwelldevelopment.spacequest.world.chunk;

import com.deepwelldevelopment.spacequest.block.Block;
import com.deepwelldevelopment.spacequest.block.Block.EnumBlockSide;

public class Layer {

    Chunk chunk;
    int y;

    Block[][] blocks;

    public Layer(Chunk chunk, int size, int y) {
        this.chunk = chunk;
        this.y = y;
        blocks = new Block[size][size];

        for (int x = 0; x < blocks.length; x++) {
            for (int z = 0; z < blocks[0].length; z++) {
                if (chunk.getHeight(x, z) > y) {
                    blocks[x][z] = new Block((chunk.x * 16) + x, y, (chunk.z * 16) + z); //part of (x, z) column below surface
                    blocks[x][z].setSidedTexture("dirt", EnumBlockSide.BOTTOM.ordinal());
                    blocks[x][z].setSidedTexture("dirt", EnumBlockSide.TOP.ordinal());
                    blocks[x][z].setSidedTexture("dirt", EnumBlockSide.FRONT.ordinal());
                    blocks[x][z].setSidedTexture("dirt", EnumBlockSide.BACK.ordinal());
                    blocks[x][z].setSidedTexture("dirt", EnumBlockSide.LEFT.ordinal());
                    blocks[x][z].setSidedTexture("dirt", EnumBlockSide.RIGHT.ordinal());
                } else if (chunk.getHeight(x, z) == y) { //surface block of (x, z) column
                    blocks[x][z] = new Block((chunk.x * 16) + x, y, (chunk.z * 16) + z);
                    blocks[x][z].setSidedTexture("dirt", EnumBlockSide.BOTTOM.ordinal());
                    blocks[x][z].setSidedTexture("grass", EnumBlockSide.TOP.ordinal());
                    blocks[x][z].setSidedTexture("dirt", EnumBlockSide.FRONT.ordinal());
                    blocks[x][z].setSidedTexture("dirt", EnumBlockSide.BACK.ordinal());
                    blocks[x][z].setSidedTexture("dirt", EnumBlockSide.LEFT.ordinal());
                    blocks[x][z].setSidedTexture("dirt", EnumBlockSide.RIGHT.ordinal());
                } else {
                    blocks[x][z] = null;
                }
            }
        }
    }

    public Block getBlock(int x, int z) {
        return blocks[x][z];
    }

    public void initBlocks() {
        for (Block[] block : blocks) {
            for (Block b : block) {
                if (b != null) {
                    b.init();
                }
            }
        }
    }

    public void render() {
        for (int x = 0; x < blocks.length; x++) {
            for (int z = 0; z < blocks[0].length; z++) {
                Block b = blocks[x][z];
                if (b != null) {
                    //in layer edge checks
                    if (x > 0 && blocks[x - 1][z] != null) {
                        b.setToDraw(false, EnumBlockSide.LEFT.ordinal());
                    }
                    if (x < blocks.length - 1 && blocks[x + 1][z] != null) {
                        b.setToDraw(false, EnumBlockSide.RIGHT.ordinal());
                    }
                    if (z > 0 && blocks[x][z - 1] != null) {
                        b.setToDraw(false, EnumBlockSide.BACK.ordinal());
                    }
                    if (z < blocks[0].length - 1 && blocks[x][z + 1] != null) {
                        b.setToDraw(false, EnumBlockSide.FRONT.ordinal());
                    }

                    //out of layer edge checks
                    if (y > 0 && chunk.getLayer(y - 1).getBlock(x, z) != null) {
                        b.setToDraw(false, EnumBlockSide.BOTTOM.ordinal());
                    }
                    if (y < chunk.getMaxHeight() - 1 && chunk.getLayer(y + 1).getBlock(x, z) != null) {
                        b.setToDraw(false, EnumBlockSide.TOP.ordinal());
                    }

                    b.draw();
                }
            }
        }
    }

    public void cleanup() {
        for (Block[] blocks1 : blocks) {
            for (Block b : blocks1) {
                if (b != null) {
                    b.cleanup();
                }
            }
        }
    }
}
