package com.deepwelldevelopment.spacequest.inventory.slot;

import com.deepwelldevelopment.spacequest.inventory.IInventory;
import com.deepwelldevelopment.spacequest.item.ItemStack;

public class Slot {

    private IInventory inventory;
    private int index;
    private int x;
    private int y;
    private int slotNumber;

    public Slot(IInventory inventory, int index, int x, int y) {
        this.inventory = inventory;
        this.index = index;
        this.x = x;
        this.y = y;
    }

    public void setSlotNumber(int slotNumber) {
        this.slotNumber = slotNumber;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public ItemStack getStack() {
        return inventory.getStackInSlot(index);
    }

    public void setStack(ItemStack stack) {
        inventory.setStackInSlot(stack, index);
    }

    public boolean isItemValid(ItemStack stack) {
        return true;
    }
}
