package com.deepwelldevelopment.spacequest.client.gui.component;

import com.badlogic.gdx.graphics.g2d.Batch;

public abstract class GuiComponent {

    private int x;
    private int y;

    public GuiComponent(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public abstract void render(Batch batch, int mouseX, int mouseY);
}
