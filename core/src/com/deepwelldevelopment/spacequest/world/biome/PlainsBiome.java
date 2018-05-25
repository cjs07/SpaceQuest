package com.deepwelldevelopment.spacequest.world.biome;

import com.deepwelldevelopment.spacequest.block.BlockProvider;

public class PlainsBiome extends Biome {
    @Override
    public int getHeight() {
        return 5;
    }

    @Override
    public double getFieldObstacleAmount() {
        return 1;
    }

    @Override
    public byte getGroundFillerBlock() {
        return BlockProvider.grass.getId();
    }

    @Override
    public byte getMountainFillerBlock() {
        return BlockProvider.grass.getId();
    }

    @Override
    public boolean hasSandBeach() {
        return false;
    }

    @Override
    public double getAmountOfWater() {
        return 5;
    }
}
