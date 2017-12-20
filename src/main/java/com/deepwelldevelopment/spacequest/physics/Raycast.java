package com.deepwelldevelopment.spacequest.physics;

import com.deepwelldevelopment.spacequest.block.Block;
import com.deepwelldevelopment.spacequest.world.World;
import org.joml.Vector3f;

import static java.lang.Math.cos;
import static java.lang.Math.floor;
import static java.lang.Math.sin;

public class Raycast {

    int startX;
    int startY;
    int startZ;
    int range;

    Vector3f direction;

    World world;

    public Raycast(int startX, int startY, int startZ, int range, World world, float hAngle, float vAngle) {
        this.startX = startX;
        this.startY = startY;
        this.startZ = startZ;
        this.range = range;
        this.world = world;

        direction = new Vector3f((float) (sin(hAngle) * cos(vAngle)), (float) (cos(vAngle)), (float) (cos(hAngle) * cos(vAngle))).normalize();
    }

    public Block getHitResult() {
        float currX = startX;
        float currY = startY;
        float currZ = startZ;
        for (int i = 0; i < range; i++) {
            Block b = world.getBlock((int)floor(currX), (int)floor(currY), (int)floor(currZ));
            if (b != null) {
                return b;
            }
            currX += direction.x;
            currY += direction.y;
            currX += direction.z;
        }
        return null;
    }
}
