package com.deepwelldevelopment.spacequest.block;

import com.badlogic.gdx.graphics.g3d.ModelInstance;

public class BlockInstance extends ModelInstance {

    public BlockInstance(Block block, BlockModel blockModel, boolean front, boolean back, boolean top, boolean bottom, boolean left, boolean right) {
        super(blockModel.createModel(front, back, top, bottom, left, right), block.getX(), block.getY(), block.getZ());
    }
}
