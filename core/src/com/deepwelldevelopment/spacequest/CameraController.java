package com.deepwelldevelopment.spacequest;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.IntIntMap;

public class CameraController extends InputAdapter {

    private int FORWARD = Keys.W;
    private int BACKWARD = Keys.S;
    private int STRAFE_LEFT = Keys.A;
    private int STRAFE_RIGHT = Keys.D;

    private final Camera camera;

    private IntIntMap keys;
    private Vector3 tmp;

    private float velocity;
    private float degreesPerPixel;

    public CameraController(Camera camera) {
        this.camera = camera;
        keys = new IntIntMap();
        tmp = new Vector3();
        velocity = 4.0f;
        degreesPerPixel = 0.25f;
        Gdx.input.setCursorCatched(true);
    }

    @Override
    public boolean keyDown(int keycode) {
        keys.put(keycode, keycode);
        return true;
    }

    @Override
    public boolean keyUp(int keycode) {
        keys.remove(keycode, 0);
        return true;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer,
            int button) {
        return super.touchDown(screenX, screenY, pointer, button);
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return super.touchUp(screenX, screenY, pointer, button);
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return mouseMoved(screenX, screenY);
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        float deltaX = -Gdx.input.getDeltaX() * degreesPerPixel;
        float deltaY = -Gdx.input.getDeltaY() * degreesPerPixel;
        camera.direction.rotate(camera.up, deltaX);
        tmp.set(camera.direction).crs(camera.up).nor();
        camera.direction.rotate(tmp, deltaY);
        return true;
    }

    @Override
    public boolean scrolled(int amount) {
        return super.scrolled(amount);
    }

    public void update(float deltaTime) {
        if (keys.containsKey(FORWARD)) {
            tmp.set(camera.direction.x, 0, camera.direction.z).nor().scl(velocity).scl(deltaTime);
            camera.translate(tmp);
        }
        if (keys.containsKey(BACKWARD)) {
            tmp.set(camera.direction.x, 0, camera.direction.z).nor().scl(-velocity).scl(deltaTime);
            camera.translate(tmp);
        }
        if (keys.containsKey(STRAFE_LEFT)) {
            tmp.set(camera.direction).crs(camera.up).nor().scl(-velocity).scl(deltaTime);
            camera.translate(tmp);
        }
        if (keys.containsKey(STRAFE_RIGHT)) {
            tmp.set(camera.direction).crs(camera.up).nor().scl(velocity).scl(deltaTime);
            camera.translate(tmp);
        }
        camera.update();
    }
}
