package com.deepwelldevelopment.spacequest.block;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.utils.Disposable;

public class BlockInstance extends ModelInstance implements Disposable {

    private Block block;
    private BlockModel blockModel;

    public BlockInstance(Block block, BlockModel blockModel, boolean front, boolean back, boolean top, boolean bottom, boolean left, boolean right) {
        super(blockModel.createModel(front, back, top, bottom, left, right), block.getX(), block.getY(), block.getZ());
        this.block = block;
        this.blockModel = blockModel;
    }

    @Override
    public void dispose() {
        blockModel.dispose();
    }
}
