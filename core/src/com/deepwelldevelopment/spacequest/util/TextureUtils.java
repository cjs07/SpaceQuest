package com.deepwelldevelopment.spacequest.util;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.deepwelldevelopment.spacequest.SpaceQuest;

public class TextureUtils {

    public static Vector2[] calculateUVMapping(String regionName) {
        Vector2[] UVList = new Vector2[4];
        TextureAtlas.AtlasRegion region = SpaceQuest.getSpaceQuest().getTextureAtlas().findRegion(regionName);

        float uOffset = region.getU();
        float vOffset = region.getV();
        float uScale = region.getU2() - uOffset;
        float vScale = region.getV2() - vOffset;

        UVList[3] = new Vector2(uOffset, vOffset); // 0,0
        UVList[0] = new Vector2(uOffset, vOffset + vScale); // 0,1
        UVList[2] = new Vector2(uOffset + uScale, vOffset); // 1,0
        UVList[1] = new Vector2(uOffset + uScale, vOffset + vScale); // 1,1

        return UVList;
    }
}
