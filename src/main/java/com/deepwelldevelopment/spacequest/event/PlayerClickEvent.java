package com.deepwelldevelopment.spacequest.event;

import com.deepwelldevelopment.spacequest.SpaceQuest;
import com.deepwelldevelopment.spacequest.block.Block;
import com.deepwelldevelopment.spacequest.physics.Raycast;
import com.deepwelldevelopment.spacequest.world.World;
import org.joml.Vector3f;

import static java.lang.Math.floor;

public class PlayerClickEvent extends Event {

    int mouseCode;

    public PlayerClickEvent(World world, int mouseCode) {
        super(world);
        this.mouseCode = mouseCode;
    }

    @Override
    public boolean dispatch() {
        Vector3f pos = SpaceQuest.INSTANCE.position;
        Raycast raycast = new Raycast((int)floor(pos.x), (int)floor(pos.y), (int)floor(pos.z), 4, world, SpaceQuest.INSTANCE.horizontalAngle,
                SpaceQuest.INSTANCE.verticalAngle);
        Block b = raycast.getHitResult();
        if (b != null) {
            if (mouseCode == 1) {
                world.setBlock(b.x, b.y, b.z, null);
            }
        }
        return dispatched;
    }
}
