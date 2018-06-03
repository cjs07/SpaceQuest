package com.deepwelldevelopment.spacequest.client.gui;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.deepwelldevelopment.spacequest.client.gui.component.GuiComponent;

import java.util.ArrayList;
import java.util.List;

public class Gui {

    private List<GuiComponent> components;

    public Gui() {
        components = new ArrayList<>();
    }

    public void render(Batch batch, int mouseX, int mouseY) {
        for (GuiComponent component : components) {
            component.render(batch, mouseX, mouseY);
        }
    }
}
