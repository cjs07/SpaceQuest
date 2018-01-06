package com.deepwelldevelopment.spacequest.physics;

import com.deepwelldevelopment.spacequest.SpaceQuest;
import com.deepwelldevelopment.spacequest.block.Block;
import com.deepwelldevelopment.spacequest.world.World;
import org.joml.Intersectionf;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class Raycast {

    float startX;
    float startY;
    float startZ;
    int range;

    Vector3f direction;

    World world;
    Vector3f min;
    Vector3f max;
    Vector2f nearFar;

    public Raycast(float startX, float startY, float startZ, int range, World world, float hAngle, float vAngle) {
        this.startX = startX;
        this.startY = startY;
        this.startZ = startZ;
        this.range = range;
        this.world = world;

        direction = new Vector3f();
        min = new Vector3f();
        max = new Vector3f();
        nearFar = new Vector2f();

//        direction = new Vector3f((float) (sin(hAngle) * cos(vAngle)), (float) (sin(vAngle)), (float) (cos(hAngle) * cos(vAngle)));
//        direction = new Vector3f();
//        SpaceQuest.INSTANCE.viewProjMatrix.transformProject(direction).normalize();
    }

    public Block getHitResult() {
        Block selectedBlock = null;
        float closesetDistance = Float.POSITIVE_INFINITY;
        direction = SpaceQuest.INSTANCE.viewProjMatrix.positiveZ(direction).negate();
        for (Block b : world.getAllBlocks()) {
            if (b != null) {
                b.setSelected(true);
                min.set(b.x, b.y, b.z);
                max.set(b.x + 1, b.y + 1, b.z + 1);
                if (Intersectionf.intersectRayAab(new Vector3f(startX, startY, startZ), direction, min, max, nearFar) && nearFar.x < closesetDistance) {
                    closesetDistance = nearFar.x;
                    selectedBlock = b;
                }
            }
        }
        if (selectedBlock != null) {
            selectedBlock.setSelected(true);
        }
        return selectedBlock;
    }
}
