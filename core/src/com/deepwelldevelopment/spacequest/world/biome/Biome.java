package com.deepwelldevelopment.spacequest.world.biome;

public abstract class Biome {

    public abstract double perlinFrequency();

    public abstract double perlinLacunarity();

    public abstract int perlinOctaves();

    public abstract double perlinPersistence();

    public abstract int getHeight();

    public abstract double getFieldObstacleAmount();

    public abstract byte getSurfaceBlock();

    public abstract byte getGroundFillerBlock();

    public abstract byte getMountainFillerBlock();

    public abstract byte getBeachBlock();

    public abstract double getAmountOfWater();

    public abstract double getCaveDensity();
}
