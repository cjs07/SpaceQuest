package com.deepwelldevelopment.spacequest.block;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.deepwelldevelopment.spacequest.client.render.block.FlowerRender;

public class BlockFlower extends Block {
    private final Color yellow = new Color(242 / 255f, 228 / 255f, 36 / 255f, 1);
    private final Color blue = new Color(77 / 255f, 3 / 255f, 213 / 255f, 1);
    private final Color red = new Color(184 / 255f, 4 / 255f, 4 / 255f, 1);
    private final Color orange = new Color(234 / 255f, 127 / 255f, 0 / 255f, 1);

    protected BlockFlower(byte id, String topTextureRegion) {
        super(id, topTextureRegion, topTextureRegion, topTextureRegion);
        this.blockRender = new FlowerRender();
        this.setOpacity(0);
        this.setCollidable(false);
    }

    @Override
    public Color getTileColor(int x, int y, int z) {
        float sinX = MathUtils.sin(x);
        float sinZ = MathUtils.sin(z);

        float v = Math.abs(sinX - sinZ);

        if (v < 0.2) {
            return yellow;
        }

        if (v < 0.4) {
            return blue;
        }

        if (v < 0.6) {
            return red;
        }

        return orange;
    }
}
