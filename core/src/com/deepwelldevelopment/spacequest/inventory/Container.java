package com.deepwelldevelopment.spacequest.inventory;

import com.deepwelldevelopment.spacequest.inventory.slot.Slot;
import com.deepwelldevelopment.spacequest.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class Container {

    protected List<ItemStack> itemstacks;
    protected List<Slot> slots;

    public Container() {
        itemstacks = new ArrayList<>();
        slots = new ArrayList<>();
    }

    public Slot addSlotToContainer(Slot slot) {
        slot.setSlotNumber(slots.size());
        slots.add(slot);
        itemstacks.add(ItemStack.EMPTY);
        return slot;
    }

    public List<ItemStack> getItemstacks() {
        return itemstacks;
    }

    public List<Slot> getSlots() {
        return slots;
    }
}
