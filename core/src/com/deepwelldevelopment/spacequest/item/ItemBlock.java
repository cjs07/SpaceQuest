package com.deepwelldevelopment.spacequest.item;

import com.deepwelldevelopment.spacequest.block.Block;
import com.deepwelldevelopment.spacequest.world.World;

public class ItemBlock extends Item {

    private Block block;

    public ItemBlock(Block block) {
        super(block.getId(), block.getTopTextureRegion());
        this.block = block;
        registerBlockMapping(block, this);
    }

    @Override
    public boolean onItemUse(World world, int x, int y, int z, float hitX, float hitY, float hitZ) {
        world.setBlock(x, y, z, block, block.isLightSource());
        return true;
    }
}
