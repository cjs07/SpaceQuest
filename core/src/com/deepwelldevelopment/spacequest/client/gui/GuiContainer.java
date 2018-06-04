package com.deepwelldevelopment.spacequest.client.gui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.deepwelldevelopment.spacequest.SpaceQuest;
import com.deepwelldevelopment.spacequest.inventory.Container;
import com.deepwelldevelopment.spacequest.inventory.slot.Slot;
import com.deepwelldevelopment.spacequest.item.ItemStack;

/**
 * Represents a GUI that displays the contents of a container or some other GUI that stores blocks
 */
public class GuiContainer extends Gui {

    private static Sprite hoveredSlotSprite;

    private Sprite backgroundSprite;
    private Container container;
    /** The itemstack attached to the mouse */
    private ItemStack mouseStack;
    private Slot mousedSlot;

    private int x;
    private int y;

    public GuiContainer(Container container, String texture) {
        if (hoveredSlotSprite == null) {
            hoveredSlotSprite = SpaceQuest.getSpaceQuest().getTextureAtlas().createSprite("gui_slot_hovered");
            hoveredSlotSprite.setSize(hoveredSlotSprite.getWidth() * GUI_SCALE, hoveredSlotSprite.getHeight() * GUI_SCALE);
        }
        this.container = container;
        this.backgroundSprite = SpaceQuest.getSpaceQuest().getTextureAtlas().createSprite(texture);
        backgroundSprite.setSize(backgroundSprite.getWidth() * GUI_SCALE, backgroundSprite.getHeight() * GUI_SCALE);
        x = (int) ((Gdx.graphics.getWidth() / 2) - (backgroundSprite.getWidth() / 2));
        y = (int) ((Gdx.graphics.getHeight() / 2) - (backgroundSprite.getHeight() / 2));
        backgroundSprite.setPosition(x, y);
        mouseStack = ItemStack.EMPTY;
    }

    @Override
    public void render(Batch batch, int mouseX, int mouseY) {
        backgroundSprite.draw(batch);
        super.render(batch, mouseX, mouseY);
        mouseY = Math.abs((mouseY - Gdx.graphics.getHeight()));
        mousedSlot = null;
        for (int i = 0; i < container.getSlots().size(); i++) {
            Slot slot = container.getSlots().get(i);
            if (slot.getStack() != ItemStack.EMPTY) {
                slot.getStack().render(batch, x + slot.getX() + (8 * GUI_SCALE) - (slot.getStack().getSprite().getWidth() / 2),
                        y + slot.getY() + (8 * GUI_SCALE) - (slot.getStack().getSprite().getHeight() / 2),
                        x + slot.getX(), y + slot.getY(), 16 * GUI_SCALE);
            }
            if (isMouseOverSlot(slot, mouseX, mouseY)) {
                hoveredSlotSprite.setPosition(x + slot.getX(), y + slot.getY());
                hoveredSlotSprite.draw(batch);
                mousedSlot = slot;
            }
        }
        if (mouseStack != null) {
            mouseStack.render(batch, mouseX, mouseY, 0, 0, 0);
        }
    }

    @Override
    public void mouseClicked(int x, int y, int button) {
        if (button == Buttons.LEFT) {
            if (mousedSlot != null) {
                ItemStack temp = mousedSlot.getStack();
                mousedSlot.setStack(mouseStack);
                mouseStack = temp;
            }
        }
    }

    private boolean isMouseOverSlot(Slot slot, int x, int y) {
        return x > this.x + slot.getX() &&
                x < this.x + slot.getX() + (16 * GUI_SCALE) &&
                y > this.y + slot.getY() &&
                y < this.y + slot.getY() + (16 * GUI_SCALE);
    }
}
