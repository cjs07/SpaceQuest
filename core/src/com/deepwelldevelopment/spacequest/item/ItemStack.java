package com.deepwelldevelopment.spacequest.item;

import com.deepwelldevelopment.spacequest.world.World;

public class ItemStack {

    public static final ItemStack EMPTY = new ItemStack(null);

    private Item item;
    private int stackSize;
    private boolean isEmpty;

    public ItemStack(Item item, int amount) {
        this.item = item;
        this.stackSize = amount;
        updateEmptyStatus();
    }

    public ItemStack(Item item) {
        this(item, 1);
    }

    private void updateEmptyStatus() {
        isEmpty = stackSize == 0;
    }

    public boolean isEmpty() {
        return isEmpty;
    }

    public Item getItem() {
        return item;
    }

    public int getStackSize() {
        return stackSize;
    }

    public int add(int amount) {
        return this.stackSize += amount;
    }

    public int remove(int amount) {
        stackSize -= amount;
        updateEmptyStatus();
        return stackSize;
    }

    public int increment() {
        return stackSize++;
    }

    public int decrement() {
        stackSize--;
        updateEmptyStatus();
        return stackSize;
    }

    public boolean onItemUse(World world, int x, int y, int z, float hitX, float hitY, float hitZ) {
        return item.onItemUse(world, x, y, z, hitX, hitY, hitZ);
    }
}
