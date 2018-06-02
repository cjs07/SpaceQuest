package com.deepwelldevelopment.spacequest.item;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.deepwelldevelopment.spacequest.SpaceQuest;
import com.deepwelldevelopment.spacequest.world.World;

public class ItemStack {

    public static final ItemStack EMPTY = new ItemStack(null);

    private Item item;
    private int stackSize;
    private boolean isEmpty;
    private Sprite sprite;

    public ItemStack(Item item, int amount) {
        this.item = item;
        this.stackSize = amount;
        updateEmptyStatus();
        if (item != null) {
            sprite = new Sprite(item.getSprite());
//            sprite.setScale(3);
        }
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

    public Sprite getSprite() {
        return sprite;
    }

    public void render(Batch batch, float x, float y, float slotX, float slotY, float slotSize) {
        if (item == null) return;
        sprite.setPosition(x, y);
        sprite.draw(batch);
        if (slotSize > 0) {
            SpaceQuest.getSpaceQuest().getFont().draw(batch, stackSize + "", slotX + slotSize, slotY + slotSize);
        }
    }
}
