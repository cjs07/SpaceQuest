package com.deepwelldevelopment.spacequest.event;

import com.deepwelldevelopment.spacequest.world.World;

public abstract class Event {

    boolean dispatched;

    World world;

    public Event (World world) {
        this.world = world;
        dispatched = false;
    }

    public abstract boolean dispatch();
}
