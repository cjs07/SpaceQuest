package com.deepwelldevelopment.spacequest;

import com.deepwelldevelopment.spacequest.Block.EnumBlockSide;

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

                blocks[x][z] = new Block(x, y, z);
                blocks[x][z].setSidedTexture("texture2.png", EnumBlockSide.BOTTOM.ordinal());
                blocks[x][z].setSidedTexture("texture.png", EnumBlockSide.TOP.ordinal());
                blocks[x][z].setSidedTexture("texture2.png", EnumBlockSide.FRONT.ordinal());
                blocks[x][z].setSidedTexture("texture2.png", EnumBlockSide.BACK.ordinal());
                blocks[x][z].setSidedTexture("texture2.png", EnumBlockSide.LEFT.ordinal());
                blocks[x][z].setSidedTexture("texture2.png", EnumBlockSide.RIGHT.ordinal());
            }
        }
    }

    public Block getBlock(int x, int z) {
        return blocks[x][z];
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
                    if (x < blocks.length-1 && blocks[x + 1][z] != null) {
                        b.setToDraw(false, EnumBlockSide.RIGHT.ordinal());
                    }
                    if (z > 0 && blocks[x][z - 1] != null) {
                        b.setToDraw(false, EnumBlockSide.FRONT.ordinal());
                    }
                    if (z < blocks[0].length-1 && blocks[x][z + 1] != null) {
                        b.setToDraw(false, EnumBlockSide.BACK.ordinal());
                    }

                    //out of layer edge checks
                    if (y > 0 && chunk.getLayer(y-1).getBlock(x, z) != null) {
                        b.setToDraw(false, EnumBlockSide.BOTTOM.ordinal());
                    }
                    if (y < chunk.getMaxHeight()-1 && chunk.getLayer(y+1).getBlock(x, z) != null) {
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
