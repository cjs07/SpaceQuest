package com.deepwelldevelopment.spacequest.inventory;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.deepwelldevelopment.spacequest.item.Item;
import com.deepwelldevelopment.spacequest.item.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InventoryPlayer implements IInventory {

    public static final int HOTBAR_SIZE = 9;

    private List<ItemStack> mainInventory;
    private List<ItemStack> hotbar;
    private List<List<ItemStack>> allInventories;
    private int selectedSlot;

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

    public ItemStack getHotbarStack(int slot) {
        return hotbar.get(slot);
    }

    public Item getHeldItem() {
        return hotbar.get(selectedSlot).getItem();
    }

    public int getSelectedSlot() {
        return selectedSlot;
    }

    public void setSelectedSlot(int selectedSlot) {
        this.selectedSlot = selectedSlot;
    }

    public List<ItemStack> getHotbar() {
        return hotbar;
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

    @Override
    public boolean addStack(ItemStack stack) {
        System.out.println("adding stack");
        if (mergeInInventory(stack, hotbar)) return true;
        if (mergeInInventory(stack, mainInventory)) return true;
        if (stack.getStackSize() > 0) { //if the stack hasnt been fully merged
            //find the first open slot in the inventory and put the stack into it
            for (List<ItemStack> inv : allInventories) {
                for (int i = 0; i < inv.size(); i++) {
                    ItemStack s = getStackInSlot(i);
                    if (s == ItemStack.EMPTY) {
                        setStackInSlot(stack, i);
                    }
                }
            }
        }
        return false;
    }

    private boolean mergeInInventory(ItemStack other, List<ItemStack> inventory) {
        for (ItemStack stack : inventory) {
            if (stack != ItemStack.EMPTY) {
                if (!stack.canMerge(stack)) {
                    continue;
                }
            }
            if (other == ItemStack.EMPTY) {
                return true; //short circuit, can't merge an empty stack into another stack
            }
            if (stack == ItemStack.EMPTY) {
                stack = other;
                other = ItemStack.EMPTY;
            } else if (stack.getItem() == other.getItem()) {
                stack.add(other.getStackSize());
                other.setStackSize(0);
                if (stack.getStackSize() > stack.getItem().getStackSizeCap()) {
                    int diff = stack.getStackSize() - stack.getItem().getStackSizeCap();
                    stack.setStackSize(stack.getItem().getStackSizeCap());
                    other.setStackSize(diff);
                }
            }
            if (stack.getStackSize() == 0) {
                return true;
            }
        }
        return false;
    }

    private void mergeStack(ItemStack stack, ItemStack other) {

    }
}
