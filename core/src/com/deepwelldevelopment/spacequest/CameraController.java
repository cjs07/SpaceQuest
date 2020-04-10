package com.deepwelldevelopment.spacequest;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.IntIntMap;
import com.deepwelldevelopment.spacequest.block.Block;
import com.deepwelldevelopment.spacequest.physics.PhysicsController;

public class CameraController extends InputAdapter {

    private int FORWARD = Keys.W;
    private int BACKWARD = Keys.S;
    private int STRAFE_LEFT = Keys.A;
    private int STRAFE_RIGHT = Keys.D;
    private int PLACE = Buttons.LEFT;
    private int BREAK = Buttons.RIGHT;

    private final Camera camera;

    private IntIntMap keys;
    private Vector3 tmp;
    private Vector3 moveVector;
    private boolean leftHeld;
    private boolean rightHeld;

    private float velocity;
    private float degreesPerPixel;

    private PhysicsController physicsController;

    private int[] breakingBlockPos;
    private long breakStart;
    private long timeLastBlockChange;


    public CameraController(Camera camera, PhysicsController physicsController) {
        this.camera = camera;
        this.physicsController = physicsController;
        keys = new IntIntMap();
        tmp = new Vector3();
        moveVector = new Vector3();
        velocity = 4.0f;
        degreesPerPixel = 0.25f;
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
            case Keys.ESCAPE:
                Gdx.app.exit();
                break;
        }
        return true;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (button == PLACE) {
            leftHeld = true;
        } else if (button == BREAK) {
            rightHeld = true;
        }
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (button == PLACE) {
            leftHeld = false;
        } else if (button == BREAK) {
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

    @Override
    public boolean scrolled(int amount) {
        return super.scrolled(amount);
    }

    public void update() {
        update(Gdx.graphics.getDeltaTime());
        movePlayer(moveVector, false);
        camera.update(true);
        //TODO: jump
    }

    public void update(float deltaTime) {
        moveVector.set(0, 0, 0);
        if (keys.containsKey(FORWARD)) {
            tmp.set(camera.direction.x, 0, camera.direction.z).nor().scl(velocity).scl(deltaTime);
            moveVector.add(tmp);
        }
        if (keys.containsKey(BACKWARD)) {
            tmp.set(camera.direction.x, 0, camera.direction.z).nor().scl(-velocity).scl(deltaTime);
            camera.translate(tmp);
            moveVector.add(tmp);
        }
        if (keys.containsKey(STRAFE_LEFT)) {
            tmp.set(camera.direction).crs(camera.up).nor().scl(-velocity).scl(deltaTime);
            camera.translate(tmp);
            moveVector.add(tmp);
        }
        if (keys.containsKey(STRAFE_RIGHT)) {
            tmp.set(camera.direction).crs(camera.up).nor().scl(velocity).scl(deltaTime);
            camera.translate(tmp);
            moveVector.add(tmp);
        }
//mouse interactions
        long currentTime = System.currentTimeMillis();
        if (leftHeld && currentTime - timeLastBlockChange > 150) {
            physicsController.rayPick(Buttons.LEFT);
            timeLastBlockChange = System.currentTimeMillis();
        }
        if (rightHeld) {
            int[] breakingPos = physicsController.rayPick(Buttons.RIGHT);
            if (breakingPos != null) {
                if (breakingBlockPos == null) {
                    breakingBlockPos = breakingPos;
                    breakStart = System.currentTimeMillis();
                }
                if (breakingPos[0] == breakingBlockPos[0] && breakingPos[1] == breakingBlockPos[1] &&
                        breakingPos[2] == breakingBlockPos[2]) { //the same block is being broken
                    Block block = SpaceQuest.getSpaceQuest().getWorld().getBlock(breakingPos[0], breakingPos[1],
                            breakingPos[2]);
                    long passedTime = System.currentTimeMillis() - breakStart;
                    if (passedTime >= block.getHardness() / 0.5f * 1000) {
                        SpaceQuest.getSpaceQuest().getWorld().breakBlock(breakingPos[0], breakingPos[1], breakingPos[2]);
//                        playerInventory.addStack(block.getDrop(breakingPos[0], breakingPos[1], breakingPos[2]));
                        breakingBlockPos = null;

                    } else { //update break state
                        float breakTime = block.getHardness() / 0.5f * 1000;
                        float stateTime = breakTime / 11;
                        int i = 0;
                        while (passedTime > stateTime) {
                            passedTime -= stateTime;
                            i++;
                        }
                        SpaceQuest.getSpaceQuest().getWorld().updateBreakState(breakingPos[0], breakingPos[1], breakingPos[2], i);
                    }
                } else {
                    breakStart = System.currentTimeMillis();
                    breakingBlockPos = breakingPos;
                }
            }
        }
        int[] temp = physicsController.rayPick(-1);
        if (temp != null) {
//            SpaceQuest.getSpaceQuest().getWorld().updateBreakState(temp[0], temp[1], temp[2], 4);
        }
    }

    protected void movePlayer(Vector3 moveVector, boolean jump) {
        physicsController.movePlayer(moveVector, jump);
    }

    public void setVelocity(float velocity) {
        this.velocity = velocity;
    }
}
