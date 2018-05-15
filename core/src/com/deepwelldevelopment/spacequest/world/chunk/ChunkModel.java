package com.deepwelldevelopment.spacequest.world.chunk;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.deepwelldevelopment.spacequest.block.Block;
import com.deepwelldevelopment.spacequest.block.BlockModel;

public class ChunkModel {

    private Chunk chunk;

    public ChunkModel(Chunk chunk) {
        this.chunk = chunk;
    }

    public Model buildModel() {
        ModelBuilder modelBuilder = new ModelBuilder();
        modelBuilder.begin();
        for (Block b : chunk.getBlockList()) {
            if (b != null) {
                int x = b.getX();
                int y = b.getY();
                int z = b.getZ();
                boolean front = false;
                boolean back = false;
                boolean top = false;
                boolean bottom = false;
                boolean left = false;
                boolean right = false;

                if (chunk.getBlock(x - 1, y, z) == null) {
                    left = true;
                }
                if (chunk.getBlock(x + 1, y, z) == null) {
                    right = true;
                }
                if (chunk.getBlock(x, y - 1, z) == null) {
                    bottom = true;
                }
                if (chunk.getBlock(x, y + 1, z) == null) {
                    top = true;
                }
                if (chunk.getBlock(x, y, z - 1) == null) {
                    front = true;
                }
                if (chunk.getBlock(x, y, z + 1) == null) {
                    back = true;
                }
                Node node = modelBuilder.node("block" + x + "," + y + "," + b.getZ(),
                        BlockModel.blockModels.get(b.getName()).createModel(front, back, top, bottom, left, right));
                node.translation.set(x, b.getY(), z);
            }
        }
        return modelBuilder.end();
    }
}
