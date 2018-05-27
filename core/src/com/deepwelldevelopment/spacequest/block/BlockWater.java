package com.deepwelldevelopment.spacequest.block;

import com.deepwelldevelopment.spacequest.client.render.block.WaterRender;
import com.deepwelldevelopment.spacequest.world.World;

public class BlockWater extends Block {

    protected BlockWater(byte id, String topTextureRegion) {
        super(id, topTextureRegion, topTextureRegion, topTextureRegion);
        this.blockRender = new WaterRender();
        this.setOpacity(24);
        this.setCollidable(false);
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z) {
        checkAndSetWater(world, x + 1, y, z);
        checkAndSetWater(world, x + 1, y, z - 1);
        checkAndSetWater(world, x + 1, y, z + 1);
        checkAndSetWater(world, x - 1, y, z);
        checkAndSetWater(world, x - 1, y, z - 1);
        checkAndSetWater(world, x - 1, y, z + 1);
        checkAndSetWater(world, x, y, z + 1);
        checkAndSetWater(world, x, y, z - 1);
    }

    @Override
    public boolean isLiquid() {
        return true;
    }

    private void checkAndSetWater(World world, int x, int y, int z) {
        Block block = world.getBlock(x, y, z);
        if (block.getId() == 0) {
            world.setBlock(x, y, z, this, false);
        }
    }
}
