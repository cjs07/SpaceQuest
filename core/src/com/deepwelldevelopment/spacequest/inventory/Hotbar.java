package com.deepwelldevelopment.spacequest.inventory;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.deepwelldevelopment.spacequest.SpaceQuest;
import com.deepwelldevelopment.spacequest.item.ItemStack;

public class Hotbar implements IInventory {

    public static final int HOTBAR_SIZE = 9;

    private Sprite unselectedSprite;
    private Sprite selectedSprite;

    private Sprite[] sprites;
    private ItemStack[] itemStacks;
    private int selectedSlot;

    public Hotbar() {
        sprites = new Sprite[HOTBAR_SIZE];
        itemStacks = new ItemStack[HOTBAR_SIZE];
        unselectedSprite = SpaceQuest.getSpaceQuest().getTextureAtlas().createSprite("hotbar_unselected");
        unselectedSprite.setScale(2, 2);
        selectedSprite = SpaceQuest.getSpaceQuest().getTextureAtlas().createSprite("hotbar_selected");
        selectedSprite.setScale(2, 2);
        int startX = (int) ((Gdx.graphics.getWidth() / 2) - ((unselectedSprite.getWidth() * unselectedSprite.getScaleX() * HOTBAR_SIZE) / 2));
        System.out.println(startX);
        Sprite sprite = new Sprite(selectedSprite);
        sprite.setPosition(startX, 50);
        sprites[0] = sprite;
        itemStacks[0] = ItemStack.EMPTY;
        for (int i = 1; i < HOTBAR_SIZE; i++) {
            sprite = new Sprite(unselectedSprite);
            sprite.setPosition(startX + (sprite.getWidth() * sprite.getScaleX() * i), 50);
            sprites[i] = sprite;
            itemStacks[i] = ItemStack.EMPTY;
        }
    }

    @Override
    public int getSize() {
        return HOTBAR_SIZE;
    }

    @Override
    public ItemStack[] getItemStacks() {
        return itemStacks;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return itemStacks[slot];
    }

    @Override
    public void setStackInSlot(ItemStack stack, int slot) {
        itemStacks[slot] = stack;
    }

    @Override
    public void render(Batch batch) {
        for (int i = 0; i < itemStacks.length; i++) {
            ItemStack stack = itemStacks[i];
            Sprite sprite = sprites[i];
            sprite.draw(batch);
            if (stack == ItemStack.EMPTY) continue;
            float itemstackX = sprite.getX() + (sprite.getWidth() / 2) - (stack.getSprite().getWidth() / 2);
            float itemstackY = sprite.getY() + (sprite.getHeight() / 2) - (stack.getSprite().getHeight() / 2);
            stack.render(batch, itemstackX, itemstackY, sprite.getX(), sprite.getY(), sprite.getWidth());
        }
    }
}
