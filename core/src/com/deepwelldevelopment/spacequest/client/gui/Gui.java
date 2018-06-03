package com.deepwelldevelopment.spacequest.client.gui;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.deepwelldevelopment.spacequest.client.gui.component.GuiComponent;

import java.util.ArrayList;
import java.util.List;

public class Gui {

    public static final float GUI_SCALE = 2;

    private List<GuiComponent> components;

    public Gui() {
        components = new ArrayList<>();
    }

    public void render(Batch batch, int mouseX, int mouseY) {
        for (GuiComponent component : components) {
            component.render(batch, mouseX, mouseY);
        }
    }

    public void mouseClicked(int x, int y, int button) {
    }

    public void keyTyped(int code) {

    }
}
