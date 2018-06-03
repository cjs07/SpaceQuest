package com.deepwelldevelopment.spacequest.inventory;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.deepwelldevelopment.spacequest.item.ItemStack;

public class PlayerInventory extends Container implements IInventory {

    private Hotbar hotbar;

    public PlayerInventory(Hotbar hotbar) {
        this.hotbar = hotbar;
    }

    @Override
    public int getSize() {
        return 0;
    }

    @Override
    public ItemStack[] getItemStacks() {
        return new ItemStack[0];
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
}
