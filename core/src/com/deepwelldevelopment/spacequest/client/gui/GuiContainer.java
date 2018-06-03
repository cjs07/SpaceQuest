package com.deepwelldevelopment.spacequest.client.gui;

import com.badlogic.gdx.Gdx;
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

    private Sprite backgroundSprite;
    private Container container;
    /** The itemstack attached to the mouse */
    private ItemStack mouseStack;

    public GuiContainer(Container container, String texture) {
        this.container = container;
        this.backgroundSprite = SpaceQuest.getSpaceQuest().getTextureAtlas().createSprite(texture);
        backgroundSprite.setScale(2);
        backgroundSprite.setPosition((Gdx.graphics.getWidth() / 2) - (backgroundSprite.getWidth() / 2),
                (Gdx.graphics.getHeight() / 2) - (backgroundSprite.getHeight() / 2));
    }

    @Override
    public void render(Batch batch, int mouseX, int mouseY) {
        backgroundSprite.draw(batch);
        super.render(batch, mouseX, mouseY);
        for (int i = 0; i < container.getSlots().size(); i++) {
            Slot slot = container.getSlots().get(i);
            slot.getStack().render(batch, slot.getX(), slot.getY(), slot.getX(), slot.getY(), 16);
        }
        if (mouseStack != null) {
            mouseStack.render(batch, mouseX, mouseY, 0, 0, 0);
        }
    }
}
