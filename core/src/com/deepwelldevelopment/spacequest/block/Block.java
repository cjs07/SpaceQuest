package com.deepwelldevelopment.spacequest.block;

/**
 * Container for model block information. Contains block data that the model is concerned with (position, type, etc),
 * but nothing about rendering
 */
public class Block {

    private int x;
    private int y;
    private int z;

    public Block(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }
}
