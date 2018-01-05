package com.deepwelldevelopment.spacequest.physics;

import com.deepwelldevelopment.spacequest.SpaceQuest;
import com.deepwelldevelopment.spacequest.block.Block;
import com.deepwelldevelopment.spacequest.world.World;
import org.joml.FrustumRayBuilder;
import org.joml.Vector3f;

import static java.lang.Math.*;

public class Raycast {

    float startX;
    float startY;
    float startZ;
    int range;

    Vector3f direction;

    World world;

    public Raycast(float startX, float startY, float startZ, int range, World world, float hAngle, float vAngle) {
        this.startX = startX;
        this.startY = startY;
        this.startZ = startZ;
        this.range = range;
        this.world = world;

        direction = new Vector3f((float) (sin(hAngle) * cos(vAngle)), (float) (sin(vAngle)), (float) (cos(hAngle) * cos(vAngle)));
//        direction = new Vector3f();
//        SpaceQuest.INSTANCE.viewProjMatrix.transformProject(direction).normalize();
    }

    public Block getHitResult() {
//        float currX = startX;
//        float currY = startY;
//        float currZ = startZ;
//        for (int i = 0; i < range; i++) {
//            System.out.println(currX + ", " + currY + ", " + currZ);
//            if (currY < 0)
//                break;
//            Block b = world.getBlock((int)floor(currX), (int)floor(currY), (int)floor(currZ));
//            if (b != null) {
//                System.out.println("raycast hit");
//                return b;
//            }
//            currX += direction.x;
//            currY += direction.y;
//            currZ += direction.z;
//        }
//        return null;

        FrustumRayBuilder rayBuilder = new FrustumRayBuilder(SpaceQuest.INSTANCE.viewProjMatrix);
        Vector3f origin = new Vector3f();
        rayBuilder.origin(origin);
        float currX = origin.x;
        float currY = origin.y;
        float currZ = origin.z;

        Vector3f direction = new Vector3f();
        rayBuilder.dir(0.5f, 0.5f, direction);

        for(int i = 0; i < range; i++) {
            System.out.println(currX + ", " + currY + ", " + currZ);
            if (currY < 0)
                break;
            Block b = world.getBlock((int)floor(currX), (int)floor(currY), (int)floor(currZ));
            if (b != null) {
                System.out.println("raycast hit");
                return b;
            }
            currX += direction.x;
            currY += direction.y;
            currZ += direction.z;
        }
        return null;
    }
}
