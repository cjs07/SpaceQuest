package com.deepwelldevelopment.spacequest.inventory;

import com.deepwelldevelopment.spacequest.client.gui.Gui;
import com.deepwelldevelopment.spacequest.inventory.slot.Slot;

public class ContainerPlayer extends Container {

    public ContainerPlayer(InventoryPlayer inventoryPlayer) {
        //inventory
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                int x = (int) (8 * (Gui.GUI_SCALE) + j * 18 * Gui.GUI_SCALE);
                int y = (int) (65 * (Gui.GUI_SCALE) - i * 18 * Gui.GUI_SCALE);
                addSlotToContainer(new Slot(inventoryPlayer, 9 + j + i * 9, x, y));
            }
        }
        //hotbar
        for (int i = 0; i < 9; i++) {
            addSlotToContainer(new Slot(inventoryPlayer, i,
                    (int) ((8 * Gui.GUI_SCALE) + (i * 18 * Gui.GUI_SCALE)),
                    (int) (7 * Gui.GUI_SCALE)
            ));
        }
    }
}
