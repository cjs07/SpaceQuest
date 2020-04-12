package com.deepwelldevelopment.spacequest.item;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.deepwelldevelopment.spacequest.SpaceQuest;
import com.deepwelldevelopment.spacequest.block.Block;
import com.deepwelldevelopment.spacequest.util.TextureUtils;

import java.util.HashMap;
import java.util.Map;

public class Item {

    protected static final Map<Block, Item> BLOCK_TO_ITEM = new HashMap<>();

    private final byte id;
    private String textureRegion;
    private Vector2[] textureUVs;
    private Sprite sprite;

    public Item(byte id, String textureRegion) {
        this.id = id;
        this.textureRegion = textureRegion;
        if (SpaceQuest.getSpaceQuest().getTextureAtlas() != null) {
            sprite = SpaceQuest.getSpaceQuest().getTextureAtlas().createSprite(textureRegion);
        }
    }

    public byte getId() {
        return id;
    }

    public static Item getItemFromBlock(Block block) {
        return BLOCK_TO_ITEM.get(block);
    }

    public Vector2[] getTextureUVs() {
        if (textureUVs == null) {
            textureUVs = TextureUtils.calculateUVMapping(textureRegion);
        }
        return textureUVs;
    }

    public Sprite getSprite() {
        return sprite;
    }

    public void setSprite(Sprite sprite) {
        this.sprite = sprite;
    }

    public static void registerBlockMapping(Block block, Item item) {
        BLOCK_TO_ITEM.put(block, item);
    }

    public int getStackSizeCap() {
        return 64;
    }
}
