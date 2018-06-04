package com.deepwelldevelopment.spacequest.inventory;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.deepwelldevelopment.spacequest.client.gui.Gui;
import com.deepwelldevelopment.spacequest.inventory.slot.Slot;
import com.deepwelldevelopment.spacequest.item.ItemStack;

public class PlayerInventory extends Container implements IInventory {

    private Hotbar hotbar;

    public PlayerInventory(Hotbar hotbar) {
        this.hotbar = hotbar;

        //inventory
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                int x = (int) ((8 * Gui.GUI_SCALE) + j * 18 * Gui.GUI_SCALE);
                int y = (int) ((65 * Gui.GUI_SCALE) - i * 18 * Gui.GUI_SCALE);
                addSlotToContainer(new Slot(this, j + i * 9 + 9, x, y));
            }
        }

        //hotbar
        for (int i = 0; i < 9; i++) {
            addSlotToContainer(new Slot(hotbar, i, (int) ((8 * Gui.GUI_SCALE) + (i * 18 * Gui.GUI_SCALE)), (int) (7 * Gui.GUI_SCALE)));
        }
    }

    @Override
    public int getSize() {
        return 36;
    }

    @Override
    public ItemStack[] getItemStacks() {
        return (ItemStack[]) this.itemstacks.toArray();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return itemstacks.get(slot);
    }

    @Override
    public void setStackInSlot(ItemStack stack, int slot) {
        this.itemstacks.set(slot, stack);
    }

    @Override
    public void render(Batch batch) {

    }
}
