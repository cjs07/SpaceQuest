package com.deepwelldevelopment.spacequest.world.biome;

public abstract class Biome {

    public abstract int getHeight();

    public abstract double getFieldObstacleAmount();

    public abstract byte getGroundFillerBlock();

    public abstract byte getMountainFillerBlock();

    public abstract boolean hasSandBeach();

    public abstract double getAmountOfWater();
}
