package com.deepwelldevelopment.spacequest.world.biome;

public class OverworldBiomeProvider implements IBiomeProvider {

    private final Biome plains = new PlainsBiome();

    @Override
    public Biome getBiomeAt(int x, int z) {
        return new Biome() {
            @Override
            public int getHeight() {
                return 0;
            }

            @Override
            public double getFieldObstacleAmount() {
                return 0;
            }

            @Override
            public byte getGroundFillerBlock() {
                return 0;
            }

            @Override
            public byte getMountainFillerBlock() {
                return 0;
            }

            @Override
            public boolean hasSandBeach() {
                return false;
            }

            @Override
            public double getAmountOfWater() {
                return 0;
            }
        };
    }
}
