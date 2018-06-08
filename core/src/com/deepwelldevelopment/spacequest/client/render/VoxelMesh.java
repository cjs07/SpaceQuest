package com.deepwelldevelopment.spacequest.client.render;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.deepwelldevelopment.spacequest.block.Block;
import com.deepwelldevelopment.spacequest.block.IBlockProvider;
import com.deepwelldevelopment.spacequest.world.chunk.Chunk;

public class VoxelMesh extends BoxMesh {

    public boolean addBlock(Vector3 worldPosition, int x, int y, int z, IBlockProvider blockProvider, Chunk chunk,
                            Block block, int breakState) {
        if (block.getId() == 0) {
            return true;
        }
        synchronized (rebuilding) {
            setupMesh();
            if (transform == null) {
                transform = new Matrix4().setTranslation(worldPosition);
                transformWithRealY = transform.cpy().translate(0, y, 0);
            }
            if (block.isCollidable()) {
                return block.getBlockRender().addBlock(worldPosition, x, y, z, blockProvider, chunk, block, vertices,
                        indices, breakState);
            } else {
                return block.getBlockRender().addBlock(worldPosition, x, y, z, blockProvider, chunk, block,
                        nonCollidableVertices, nonCollidableIndicies, breakState);
            }
        }
    }

    public boolean addBlock(Vector3 worldPosition, int x, int y, int z, IBlockProvider blockProvider, Chunk chunk,
                            Block block) {
        return addBlock(worldPosition, x, y, z, blockProvider, chunk, block, 0);
    }
}
