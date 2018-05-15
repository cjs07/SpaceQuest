package com.deepwelldevelopment.spacequest.world;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.deepwelldevelopment.spacequest.world.chunk.Chunk;
import com.deepwelldevelopment.spacequest.world.chunk.ChunkModel;

public class WorldModel {

    private World world;

    public WorldModel(World world) {
        this.world = world;
    }

    public Model buildModel() {
        ModelBuilder modelBuilder = new ModelBuilder();
        modelBuilder.begin();
        for (Chunk c : world.getChunks()) {
            modelBuilder.node("chunk" + c.getX() + "," + c.getZ(), new ChunkModel(c).buildModel());
        }
        return modelBuilder.end();
    }
}
