package com.deepwelldevelopment.spacequest.inventory;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.deepwelldevelopment.spacequest.item.ItemStack;

public class InventoryCrafting implements IInventory {
    @Override
    public int getSize() {
        return 10;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return null;
    }

    @Override
    public void setStackInSlot(ItemStack stack, int slot) {

    }

    @Override
    public void render(Batch batch) {

    }

    @Override
    public boolean addStack(ItemStack stack) {
        return false;
    }
}
