package com.deepwelldevelopment.spacequest;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.IntIntMap;
import com.deepwelldevelopment.spacequest.physics.PhysicsController;

import static com.badlogic.gdx.Input.Keys.Q;
import static com.badlogic.gdx.Input.Keys.SPACE;

public class CameraController extends InputAdapter {

    private final Camera camera;
    private final IntIntMap keys = new IntIntMap();
    private final Vector3 tmp = new Vector3();
    private int STRAFE_LEFT = Keys.A;
    private int STRAFE_RIGHT = Keys.D;
    private int FORWARD = Keys.W;
    private int BACKWARD = Keys.S;
    private int UP = Keys.SPACE;
    private int DOWN = Keys.SHIFT_LEFT;
    private int JUMP = SPACE;
    private int LMB = Buttons.LEFT;
    private int RMB = Buttons.RIGHT;
    private float velocity = 4.0f;
    private float degreesPerPixel = 0.25f;
    private boolean leftHeld;
    private boolean rightHeld;
    private long timeLastBlockChange;
    private boolean jump;
    private Vector3 moveVector = new Vector3();

    private PhysicsController physicsController;

    public CameraController(Camera camera, PhysicsController physicsController) {
        this.camera = camera;
        this.physicsController = physicsController;
    }

    @Override
    public boolean keyDown(int keycode) {
        keys.put(keycode, keycode);
        return true;
    }

    @Override
    public boolean keyUp(int keycode) {
        keys.remove(keycode, 0);
        switch (keycode) {
            case Q:
                Gdx.app.exit();
        }
        return true;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (button == LMB) {
            leftHeld = true;
        } else if (button == RMB) {
            rightHeld = true;
        }
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (button == LMB) {
            leftHeld = false;
        } else if (button == RMB) {
            rightHeld = false;
        }
        return true;
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

    /**
     * Sets the velocity in units per second for moving forward, backward and strafing left/right.
     *
     * @param velocity the velocity in units per second
     */
    public void setVelocity(float velocity) {
        this.velocity = velocity / 10;
    }

    /**
     * Sets how many degrees to rotate per pixel the mouse moved.
     *
     * @param degreesPerPixel
     */
    public void setDegreesPerPixel(float degreesPerPixel) {
        this.degreesPerPixel = degreesPerPixel;
    }

    public void update() {
        update(Gdx.graphics.getDeltaTime());
        movePlayer(moveVector, jump);
        camera.update(true);
        jump = false;
    }

    public void update(float deltaTime) {
        moveVector.set(0, 0, 0);
        if (keys.containsKey(FORWARD)) {
//            tmp.set(camera.direction.x, 0, camera.direction.z).nor().scl(velocity * deltaTime);
            tmp.set(camera.direction.x, 0, camera.direction.z).nor().scl(velocity);
            moveVector.add(tmp);
        }
        if (keys.containsKey(BACKWARD)) {
//            tmp.set(camera.direction.x, 0, camera.direction.z).nor().scl(-velocity * deltaTime);
            tmp.set(camera.direction.x, 0, camera.direction.z).nor().scl(-velocity);
            moveVector.add(tmp);
        }
        if (keys.containsKey(STRAFE_LEFT)) {
//            tmp.set(camera.direction).crs(camera.up).nor().scl(-velocity * deltaTime).y = 0;
            tmp.set(camera.direction).crs(camera.up).nor().scl(-velocity).y = 0;
            moveVector.add(tmp);
        }
        if (keys.containsKey(STRAFE_RIGHT)) {
//            tmp.set(camera.direction).crs(camera.up).nor().scl(velocity * deltaTime).y = 0;
            tmp.set(camera.direction).crs(camera.up).nor().scl(velocity).y = 0;
            moveVector.add(tmp);
        }
        if (keys.containsKey(UP)) {
//            tmp.set(camera.up).nor().scl(velocity * deltaTime);
            tmp.set(camera.up).nor().scl(velocity);
            moveVector.add(tmp);
            jump = true;
        }
        if (keys.containsKey(DOWN)) {
//            tmp.set(camera.up).nor().scl(-velocity * deltaTime);
            tmp.set(camera.up).nor().scl(-velocity);
            moveVector.add(tmp);
        }

        //mouse interactions
        long currentTime = System.currentTimeMillis();
        if (leftHeld && currentTime - timeLastBlockChange > 150) {
            physicsController.rayPick(Buttons.LEFT);
            timeLastBlockChange = System.currentTimeMillis();
        }
        if (rightHeld && currentTime - timeLastBlockChange > 150) {
            physicsController.rayPick(Buttons.RIGHT);
            timeLastBlockChange = System.currentTimeMillis();
        }
    }

    protected void movePlayer(Vector3 moveVector, boolean jump) {
        physicsController.movePlayer(moveVector, jump);
//        camera.position.add(moveVector);
    }
}
