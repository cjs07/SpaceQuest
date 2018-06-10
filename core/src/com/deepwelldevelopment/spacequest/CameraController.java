package com.deepwelldevelopment.spacequest;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.IntIntMap;
import com.deepwelldevelopment.spacequest.block.Block;
import com.deepwelldevelopment.spacequest.block.BlockProvider;
import com.deepwelldevelopment.spacequest.client.gui.Gui;
import com.deepwelldevelopment.spacequest.client.gui.GuiContainer;
import com.deepwelldevelopment.spacequest.inventory.ContainerPlayer;
import com.deepwelldevelopment.spacequest.inventory.Hotbar;
import com.deepwelldevelopment.spacequest.inventory.InventoryPlayer;
import com.deepwelldevelopment.spacequest.physics.PhysicsController;

import static com.badlogic.gdx.Input.Keys.*;

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
    private int HOTBAR_1 = Keys.NUM_1;
    private int HOTBAR_2 = Keys.NUM_2;
    private int HOTBAR_3 = Keys.NUM_3;
    private int HOTBAR_4 = Keys.NUM_4;
    private int HOTBAR_5 = Keys.NUM_5;
    private int HOTBAR_6 = Keys.NUM_6;
    private int HOTBAR_7 = Keys.NUM_7;
    private int HOTBAR_8 = Keys.NUM_8;
    private int HOTBAR_9 = Keys.NUM_9;
    private int JUMP = SPACE;
    private int INVENTORY = Keys.E;
    private int LMB = Buttons.LEFT;
    private int RMB = Buttons.RIGHT;
    private int SCROLL_SENSITIVITY = 1;
    private float velocity = 4.0f;
    private float degreesPerPixel = 0.25f;
    private boolean leftHeld;
    private boolean rightHeld;
    private long timeLastBlockChange;
    private boolean jump;
    private Vector3 moveVector = new Vector3();
    private boolean fullscreen;
    private boolean cursorCatch;

    private PhysicsController physicsController;
    private InventoryPlayer playerInventory;
    private Gui inventoryGui;
    private int[] breakingBlockPos;
    private long breakStart;

    public CameraController(Camera camera, PhysicsController physicsController) {
        this.camera = camera;
        this.physicsController = physicsController;
        playerInventory = new InventoryPlayer();
        inventoryGui = new GuiContainer(new ContainerPlayer(playerInventory, SpaceQuest.getSpaceQuest().getHotbar()), "gui_inventory");
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
                break;
            case F:
                if (fullscreen) {
                    Gdx.graphics.setWindowedMode(1280, 768);
                    fullscreen = false;
                } else {
                    Graphics.DisplayMode desktopDisplayMode = Gdx.graphics.getDisplayMode(
                            Gdx.graphics.getPrimaryMonitor());
                    Gdx.graphics.setFullscreenMode(desktopDisplayMode);
                    fullscreen = true;
                }
                break;
            case ESCAPE:
                if (SpaceQuest.getSpaceQuest().isGuiOpen()) {
                    SpaceQuest.getSpaceQuest().closeGui();
                } else {
                    if (cursorCatch) {
                        releaseCursor();
                    } else {
                        catchCursor();
                    }
                }
                break;
        }

        if (SpaceQuest.getSpaceQuest().isGuiOpen()) {
            SpaceQuest.getSpaceQuest().getOpenGui().keyTyped(keycode);
        } else {
            if (keycode == HOTBAR_1) {
                SpaceQuest.getSpaceQuest().getHotbar().setSelectedSlot(0);
            } else if (keycode == HOTBAR_2) {
                SpaceQuest.getSpaceQuest().getHotbar().setSelectedSlot(1);
            } else if (keycode == HOTBAR_3) {
                SpaceQuest.getSpaceQuest().getHotbar().setSelectedSlot(2);
            } else if (keycode == HOTBAR_4) {
                SpaceQuest.getSpaceQuest().getHotbar().setSelectedSlot(3);
            } else if (keycode == HOTBAR_5) {
                SpaceQuest.getSpaceQuest().getHotbar().setSelectedSlot(4);
            } else if (keycode == HOTBAR_6) {
                SpaceQuest.getSpaceQuest().getHotbar().setSelectedSlot(5);
            } else if (keycode == HOTBAR_7) {
                SpaceQuest.getSpaceQuest().getHotbar().setSelectedSlot(6);
            } else if (keycode == HOTBAR_8) {
                SpaceQuest.getSpaceQuest().getHotbar().setSelectedSlot(7);
            } else if (keycode == HOTBAR_9) {
                SpaceQuest.getSpaceQuest().getHotbar().setSelectedSlot(8);
            } else if (keycode == INVENTORY) {
                SpaceQuest.getSpaceQuest().openGui(inventoryGui);
            }
        }
        return true;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (SpaceQuest.getSpaceQuest().isGuiOpen()) {

        } else {
            if (button == LMB) {
                leftHeld = true;
            } else if (button == RMB) {
                rightHeld = true;
            }
        }
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (SpaceQuest.getSpaceQuest().isGuiOpen()) {
            SpaceQuest.getSpaceQuest().getOpenGui().mouseClicked(screenX, screenY, button);
        } else {
            if (button == LMB) {
                leftHeld = false;
            } else if (button == RMB) {
                rightHeld = false;
            }
        }
        return true;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return mouseMoved(screenX, screenY);
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        if (!SpaceQuest.getSpaceQuest().isGuiOpen()) {
            float deltaX = -Gdx.input.getDeltaX() * degreesPerPixel;
            float deltaY = -Gdx.input.getDeltaY() * degreesPerPixel;
            camera.direction.rotate(camera.up, deltaX);
            tmp.set(camera.direction).crs(camera.up).nor();
            camera.direction.rotate(tmp, deltaY);
        }
        return true;
    }

    @Override
    public boolean scrolled(int amount) {
        int num = amount / SCROLL_SENSITIVITY;
        Hotbar hotbar = SpaceQuest.getSpaceQuest().getHotbar();
        int newSelected = hotbar.getSelectedSlot() + num;
        while (newSelected >= 9) {
            newSelected -= 9;
        }
        while (newSelected < 0) {
            newSelected += 9;
        }
        hotbar.setSelectedSlot(newSelected);
        return true;
    }

    public void releaseCursor() {
        Gdx.input.setCursorCatched(false);
        cursorCatch = false;
    }

    public void catchCursor() {
        Gdx.input.setCursorCatched(true);
        cursorCatch = true;
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
//            tmp.set(camera.up).nor().scl(velocity);
//            moveVector.add(tmp);
            jump = true;
        }
        if (keys.containsKey(DOWN)) {
//            tmp.set(camera.up).nor().scl(-velocity * deltaTime);
//            tmp.set(camera.up).nor().scl(-velocity);
//            moveVector.add(tmp);
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
                }
                if (breakingPos[0] == breakingBlockPos[0] && breakingPos[1] == breakingBlockPos[1] &&
                        breakingPos[2] == breakingBlockPos[2]) { //the same block is being broken
                    Block block = SpaceQuest.getSpaceQuest().getWorld().getBlock(breakingPos[0], breakingPos[1],
                            breakingPos[2]);
                    long passedTime = System.currentTimeMillis() - breakStart;
                    if (passedTime >= block.getHardness() / 0.5f * 1000) {
                        SpaceQuest.getSpaceQuest().getWorld().setBlock(breakingPos[0], breakingPos[1], breakingPos[2],
                                BlockProvider.air, false);
                    } else { //update break state
                        float breakTime = block.getHardness() / 0.5f * 1000;
                        float stateTime = breakTime / 11;
                        int i = 0;
                        while (passedTime > stateTime) {
                            passedTime -= stateTime;
                            i++;
                        }
                        System.out.println(i);
                        SpaceQuest.getSpaceQuest().getWorld().updateBreakState(breakingPos[0], breakingPos[1], breakingPos[2], i);

                    }
                } else {
                    breakStart = System.currentTimeMillis();
                    breakingBlockPos = breakingPos;
                }
            }
        }
        timeLastBlockChange = System.currentTimeMillis();
        int[] temp = physicsController.rayPick(-1);
        if (temp != null) {
//            SpaceQuest.getSpaceQuest().getWorld().updateBreakState(temp[0], temp[1], temp[2], 4);
        }
    }


    protected void movePlayer(Vector3 moveVector, boolean jump) {
        physicsController.movePlayer(moveVector, jump);
//        camera.position.add(moveVector);
    }
}
