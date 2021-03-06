package com.deepwelldevelopment.spacequest.client.render.block;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.ShortArray;
import com.deepwelldevelopment.spacequest.block.Block;
import com.deepwelldevelopment.spacequest.block.IBlockProvider;
import com.deepwelldevelopment.spacequest.world.chunk.Chunk;

public class WaterRender extends BasicBlockRender {

    @Override
    public synchronized boolean addBlock(Vector3 worldPosition, int x, int y, int z, IBlockProvider blockProvider, Chunk chunk, Block block, FloatArray vertices, ShortArray indicies, int breakState) {
        Vector2[] topTextureUVs = block.getTopTextureUVs();
        if (chunk.getBlock(x, y + 1, z) == 0) {
            points[0] = pointVector0.set(x, y + 0.69f, z + 1);        //BOTTOM
            points[1] = pointVector1.set(x + 1, y + 0.69f, z + 1);    //BOTTOM
            points[2] = pointVector2.set(x + 1, y + 0.7f, z + 1);   //TOP
            points[3] = pointVector3.set(x, y + 0.7f, z + 1);       //TOP
            points[4] = pointVector4.set(x + 1, y + 0.69f, z);        //BOTTOM
            points[5] = pointVector5.set(x, y + 0.69f, z);            //BOTTOM
            points[6] = pointVector6.set(x, y + 0.7f, z);           //TOP
            points[7] = pointVector7.set(x + 1, y + 0.7f, z);       //TOP

            resetLight();
            setAOLightTop(x, y, z, chunk, block);
            setTexCoords(topTextureUVs);

            addTop(vertices, indicies);
            addBottom(vertices, indicies);
        }
        return true;
    }
}
