package com.deepwelldevelopment.spacequest.client.gui;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;

/**
 * Represents a GUI that displays the contents of a container or some other gui that stores blocks
 */
public class GuiContainer extends Gui {

    private Sprite backgroundSprite;

    @Override
    public void render(Batch batch, int mouseX, int mouseY) {
        backgroundSprite.draw(batch);
        super.render(batch, mouseX, mouseY);
    }
}
