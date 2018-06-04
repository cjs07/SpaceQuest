package com.deepwelldevelopment.spacequest.inventory;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.deepwelldevelopment.spacequest.item.ItemStack;

public interface IInventory {

    int getSize();

    ItemStack getStackInSlot(int slot);

    void setStackInSlot(ItemStack stack, int slot);

    void render(Batch batch);
}
