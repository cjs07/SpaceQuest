package com.deepwelldevelopment.spacequest.world.biome;

public class OverworldBiomeProvider implements IBiomeProvider {

    private final Biome plains = new PlainsBiome();

    @Override
    public Biome getBiomeAt(int x, int z) {
        return plains;
    }
}
