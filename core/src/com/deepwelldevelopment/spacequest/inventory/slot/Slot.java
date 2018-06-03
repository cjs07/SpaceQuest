package com.deepwelldevelopment.spacequest.inventory.slot;

import com.deepwelldevelopment.spacequest.inventory.IInventory;
import com.deepwelldevelopment.spacequest.item.ItemStack;

public class Slot {

    private IInventory inventory;
    private int index;
    private int x;
    private int y;
    private ItemStack stack;
    private int slotNumber;

    public Slot(IInventory inventory, int index, int x, int y) {
        this.inventory = inventory;
        this.index = index;
        this.x = x;
        this.y = y;
        stack = inventory.getStackInSlot(index);
        if (stack == null) {
            stack = ItemStack.EMPTY;
        }
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
        return stack;
    }

    public void setStack(ItemStack stack) {
        this.stack = stack;
    }

    public boolean isItemValid(ItemStack itemStack) {
        return true;
    }
}
