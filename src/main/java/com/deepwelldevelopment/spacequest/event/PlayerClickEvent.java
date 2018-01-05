package com.deepwelldevelopment.spacequest.event;

import com.deepwelldevelopment.spacequest.SpaceQuest;
import com.deepwelldevelopment.spacequest.block.Block;
import com.deepwelldevelopment.spacequest.physics.Raycast;
import com.deepwelldevelopment.spacequest.world.World;
import org.joml.Vector3f;

public class PlayerClickEvent extends Event {

    int mouseCode;

    public PlayerClickEvent(World world, int mouseCode) {
        super(world);
        this.mouseCode = mouseCode;
    }

    @Override
    public boolean dispatch() {
        Vector3f pos = SpaceQuest.INSTANCE.cameraPosition;
        Raycast raycast = new Raycast(pos.x, pos.y, pos.z, 25, world, SpaceQuest.INSTANCE.horizontalAngle,
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
