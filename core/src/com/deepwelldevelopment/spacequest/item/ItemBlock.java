package com.deepwelldevelopment.spacequest.item;

import com.deepwelldevelopment.spacequest.block.Block;

public class ItemBlock extends Item {
    public ItemBlock(Block block) {
        super(block.getId(), block.getTopTextureRegion());
    }
}
