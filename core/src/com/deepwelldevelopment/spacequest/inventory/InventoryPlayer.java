package com.deepwelldevelopment.spacequest.inventory;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.deepwelldevelopment.spacequest.item.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InventoryPlayer implements IInventory {

    private List<ItemStack> mainInventory;
    private List<ItemStack> hotbar;
    private List<List<ItemStack>> allInventories;

    public InventoryPlayer() {
        mainInventory = new ArrayList<>(27);
        hotbar = new ArrayList<>(9);
        for (int i = 0; i < 27; i++) {
            mainInventory.add(i, ItemStack.EMPTY);
        }
        for (int i = 0; i < 9; i++) {
            hotbar.add(i, ItemStack.EMPTY);
        }
        allInventories = Arrays.asList(hotbar, mainInventory);
    }

    @Override
    public int getSize() {
        return 36;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        List<ItemStack> list = null;
        for (List<ItemStack> l : allInventories) {
            if (slot < l.size()) {
                list = l;
                break;
            }
            slot -= l.size();
        }
        if (list != null) {
            return list.get(slot);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void setStackInSlot(ItemStack stack, int slot) {
        List<ItemStack> list = null;
        for (List<ItemStack> l : allInventories) {
            if (slot < l.size()) {
                list = l;
                break;
            }
            slot -= l.size();
        }
        if (list != null) {
            list.set(slot, stack);
        }
    }

    @Override
    public void render(Batch batch) {

    }
}
