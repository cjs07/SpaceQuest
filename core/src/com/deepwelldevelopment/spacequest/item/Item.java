package com.deepwelldevelopment.spacequest.item;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.deepwelldevelopment.spacequest.util.TextureUtils;
import com.deepwelldevelopment.spacequest.world.World;

public class Item {

    private final byte id;
    private String textureRegion;
    private Vector2[] textureUVs;
    private Sprite sprite;

    public Item(byte id, String textureRegion) {
        this.id = id;
        this.textureRegion = textureRegion;
    }

    public byte getId() {
        return id;
    }

    public Vector2[] getTextureUVs() {
        if (textureUVs == null) {
            textureUVs = TextureUtils.calculateUVMapping(textureRegion);
        }
        return textureUVs;
    }

    public boolean onItemUse(World world, int x, int y, int z, float hitX, float hitY, float hitZ) {
        return false;
    }

    public Sprite getSprite() {
        return sprite;
    }

    public void setSprite(Sprite sprite) {
        this.sprite = sprite;
    }
}
