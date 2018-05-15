package com.deepwelldevelopment.spacequest.block;

/**
 * Container for model block information. Contains block data that the model (system, not rendering) is concerned with
 * (position, type, etc), but nothing about rendering
 */
public class Block {

    private String name;
    private int x;
    private int y;
    private int z;

    public Block(String name, int x, int y, int z) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public String getName() {
        return name;
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
