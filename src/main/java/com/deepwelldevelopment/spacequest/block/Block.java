package com.deepwelldevelopment.spacequest.block;

/**
 * Represents a block in the game. Blocks act as base data containers, but do
 * not actually represent an instance of a block in the world.
 */
public class Block {

    private final byte id;

    private int opactiy;
    private boolean isLightSource;
    private boolean collidable;
    private float hardness;

    public Block(byte id) {
        this.id = id;
    }

    public boolean onBlockActivated(int x, int y, int z) {
        return false;
    }

    public int getOpactiy() {
        return opactiy;
    }

    public Block setOpactiy(int opactiy) {
        this.opactiy = opactiy;
        return this;
    }

    public boolean isLightSource() {
        return isLightSource;
    }

    public Block setLightSource(boolean lightSource) {
        isLightSource = lightSource;
        return this;
    }

    public boolean isCollidable() {
        return collidable;
    }

    public Block setCollidable(boolean collidable) {
        this.collidable = collidable;
        return this;
    }

    public float getHardness() {
        return hardness;
    }

    public Block setHardness(float hardness) {
        this.hardness = hardness;
        return this;
    }

    public boolean isLiquid() {
        return false;
    }
}
