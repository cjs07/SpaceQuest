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
            sprite.setSize(sprite.getWidth() * 0.8f, sprite.getHeight() * 0.8f);
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

    public void setStackSize(int stackSize) {
        this.stackSize = stackSize;
        updateEmptyStatus();
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

    public ItemStack merge(ItemStack other) {
        if (other == EMPTY) {
            return this; //short circuit, can't merge an empty stack into another stack
        }
        if (this == EMPTY) {
            return new ItemStack(other.item, other.stackSize);
        } else if (this.item == other.item) {
            this.stackSize += other.stackSize;
            other.stackSize = 0;
            if (this.stackSize > this.item.getStackSizeCap()) {
                int diff = this.stackSize - this.item.getStackSizeCap();
                this.stackSize = this.item.getStackSizeCap();
                other.stackSize = diff;
            }
            return this;
        }

        return EMPTY;
    }

    public boolean canMerge(ItemStack other) {
        if (this == EMPTY || other == EMPTY) return true;
        return this.item == other.item && this.stackSize < this.item.getStackSizeCap();
    }

    public Sprite getSprite() {
        return sprite;
    }

    public void render(Batch batch, float x, float y, float slotX, float slotY, float slotSize) {
        if (item == null) return;
        if (slotSize > 0) {
            sprite.setPosition(x, y);
            sprite.draw(batch);
            SpaceQuest.getSpaceQuest().getFont().draw(batch, stackSize + "", slotX + slotSize, slotY + slotSize);
        } else {
            sprite.setOriginBasedPosition(x, y);
            sprite.draw(batch);
        }
    }
}
