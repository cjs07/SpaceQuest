package com.deepwelldevelopment.spacequest.inventory;

import com.deepwelldevelopment.spacequest.inventory.slot.Slot;
import com.deepwelldevelopment.spacequest.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class Container {

    protected List<ItemStack> itemStacks;
    protected List<Slot> slots;

    public Container() {
        itemStacks = new ArrayList<>();
        slots = new ArrayList<>();
    }

    public Slot addSlotToContainer(Slot slot) {
        slot.setSlotNumber(slots.size());
        slots.add(slot);
        itemStacks.add(ItemStack.EMPTY);
        return slot;
    }

    public List<ItemStack> getItemStacks() {
        return itemStacks;
    }

    public List<Slot> getSlots() {
        return slots;
    }
}
