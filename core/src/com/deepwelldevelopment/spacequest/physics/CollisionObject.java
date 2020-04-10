package com.deepwelldevelopment.spacequest.physics;

import com.badlogic.gdx.math.Vector3;

import java.util.ArrayList;
import java.util.Collection;

public class CollisionObject {

    private final Collection<PositionListener> listeners = new ArrayList<>();
    private Vector3 velocity;

    public void setPosition(Vector3 position) {
        for (PositionListener listener : listeners) {
            listener.onPositionChange(position);
        }
    }

    public void moveObject(Vector3 velocity) {
        this.velocity.set(velocity);
    }

    public Vector3 getVelocity() {
        return velocity;
    }

    public void resetVelocity() {
        velocity.set(Vector3.Zero);
    }

    public void addListener(PositionListener listener) {
        listeners.add(listener);
    }

    public interface PositionListener {
        void onPositionChange(Vector3 position);
    }
}
