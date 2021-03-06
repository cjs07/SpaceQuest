package com.deepwelldevelopment.spacequest.world.biome;

import com.deepwelldevelopment.spacequest.block.BlockProvider;

public class PlainsBiome extends Biome {
    @Override
    public double perlinFrequency() {
        return 0.15;
    }

    @Override
    public double perlinLacunarity() {
        return 2;
    }

    @Override
    public int perlinOctaves() {
        return 6;
    }

    @Override
    public double perlinPersistence() {
        return 0.5;
    }

    @Override
    public int getHeight() {
        return 47;
    }

    @Override
    public double getFieldObstacleAmount() {
        return 1;
    }

    @Override
    public byte getSurfaceBlock() {
        return BlockProvider.grass.getId();
    }

    @Override
    public byte getGroundFillerBlock() {
        return BlockProvider.dirt.getId();
    }

    @Override
    public byte getMountainFillerBlock() {
        return BlockProvider.dirt.getId();
    }

    @Override
    public byte getBeachBlock() {
        return BlockProvider.dirt.getId();
    }

    @Override
    public double getAmountOfWater() {
        return 5;
    }

    @Override
    public double getCaveDensity() {
        return 0.1;
    }
}
