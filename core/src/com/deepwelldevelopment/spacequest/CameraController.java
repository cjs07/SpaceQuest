package com.deepwelldevelopment.spacequest;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.IntIntMap;
import com.deepwelldevelopment.spacequest.block.Block;
import com.deepwelldevelopment.spacequest.client.gui.Gui;
import com.deepwelldevelopment.spacequest.client.gui.GuiContainer;
import com.deepwelldevelopment.spacequest.inventory.ContainerPlayer;
import com.deepwelldevelopment.spacequest.inventory.InventoryPlayer;
import com.deepwelldevelopment.spacequest.physics.PhysicsController;

import static com.badlogic.gdx.Input.Keys.*;

public class CameraController extends InputAdapter {

    private int FORWARD = Keys.W;
    private int BACKWARD = Keys.S;
    private int STRAFE_LEFT = Keys.A;
    private int STRAFE_RIGHT = Keys.D;
    private int UP = Keys.SPACE;
    private int DOWN = Keys.SHIFT_LEFT;
    private int PLACE = Buttons.LEFT;
    private int BREAK = Buttons.RIGHT;
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
    private int SCROLL_SENSITIVITY = 1;

    private final Camera camera;

    private IntIntMap keys;
    private Vector3 tmp;
    private Vector3 moveVector;
    private boolean leftHeld;
    private boolean rightHeld;

    private boolean fullscreen;
    private boolean cursorCatch;

    private float velocity;
    private float degreesPerPixel;
    private boolean jump;

    private PhysicsController physicsController;
    private InventoryPlayer playerInventory;
    private Gui inventoryGui;

    private int[] breakingBlockPos;
    private long breakStart;
    private long timeLastBlockChange;


    public CameraController(Camera camera, PhysicsController physicsController,
            InventoryPlayer playerInventory) {
        this.camera = camera;
        this.physicsController = physicsController;
        this.playerInventory = playerInventory;
        inventoryGui = new GuiContainer(new ContainerPlayer(playerInventory), "gui_inventory");
        keys = new IntIntMap();
        tmp = new Vector3();
        moveVector = new Vector3();
        velocity = 4.0f;
        degreesPerPixel = 0.25f;
        Gdx.input.setCursorPosition(0, 0);
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
                    DisplayMode desktopDisplayMode =
                            Gdx.graphics.getDisplayMode(Gdx.graphics.getMonitor());
                    Gdx.graphics.setFullscreenMode(desktopDisplayMode);
                    fullscreen = true;
                }
                break;
            case ESCAPE:
                if (SpaceQuest.getSpaceQuest().isGuiOen()) {
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

        if (SpaceQuest.getSpaceQuest().isGuiOen()) {
            SpaceQuest.getSpaceQuest().getOpenGui().keyTyped(keycode);
        } else {
            if (keycode == HOTBAR_1) {
                SpaceQuest.getSpaceQuest().getPlayerInventory().setSelectedSlot(0);
            } else if (keycode == HOTBAR_2) {
                SpaceQuest.getSpaceQuest().getPlayerInventory().setSelectedSlot(1);
            } else if (keycode == HOTBAR_3) {
                SpaceQuest.getSpaceQuest().getPlayerInventory().setSelectedSlot(2);
            } else if (keycode == HOTBAR_4) {
                SpaceQuest.getSpaceQuest().getPlayerInventory().setSelectedSlot(3);
            } else if (keycode == HOTBAR_5) {
                SpaceQuest.getSpaceQuest().getPlayerInventory().setSelectedSlot(4);
            } else if (keycode == HOTBAR_6) {
                SpaceQuest.getSpaceQuest().getPlayerInventory().setSelectedSlot(5);
            } else if (keycode == HOTBAR_7) {
                SpaceQuest.getSpaceQuest().getPlayerInventory().setSelectedSlot(6);
            } else if (keycode == HOTBAR_8) {
                SpaceQuest.getSpaceQuest().getPlayerInventory().setSelectedSlot(7);
            } else if (keycode == HOTBAR_9) {
                SpaceQuest.getSpaceQuest().getPlayerInventory().setSelectedSlot(8);
            } else if (keycode == INVENTORY) {
                SpaceQuest.getSpaceQuest().openGui(inventoryGui);
            }
        }
        return true;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (SpaceQuest.getSpaceQuest().isGuiOen()) {
        } else {
            if (button == PLACE) {
                leftHeld = true;
            } else if (button == BREAK) {
                rightHeld = true;
            }
        }
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (SpaceQuest.getSpaceQuest().isGuiOen()) {
            SpaceQuest.getSpaceQuest().getOpenGui().mouseClicked(screenX, screenY, button);
        } else {
            if (button == PLACE) {
                leftHeld = false;
            } else if (button == BREAK) {
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
        if (!SpaceQuest.getSpaceQuest().isGuiOen()) {
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
        int newSelected = playerInventory.getSelectedSlot() + num;
        while (newSelected >= 9) {
            newSelected -= 9;
        }
        while (newSelected < 0) {
            newSelected += 9;
        }
        playerInventory.setSelectedSlot(newSelected);
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

    public void update() {
        update(Gdx.graphics.getDeltaTime());
        movePlayer(moveVector, jump);
        camera.update(true);
        jump = false;
    }

    public void update(float deltaTime) {
        moveVector.set(0, 0, 0);
        if (keys.containsKey(FORWARD)) {
            tmp.set(camera.direction.x, 0, camera.direction.z).nor().scl(velocity);
            moveVector.add(tmp);
        }
        if (keys.containsKey(BACKWARD)) {
            tmp.set(camera.direction.x, 0, camera.direction.z).nor().scl(-velocity);
            moveVector.add(tmp);
        }
        if (keys.containsKey(STRAFE_LEFT)) {
            tmp.set(camera.direction).crs(camera.up).nor().scl(-velocity).y = 0;
            moveVector.add(tmp);
        }
        if (keys.containsKey(STRAFE_RIGHT)) {
            tmp.set(camera.direction).crs(camera.up).nor().scl(velocity).y = 0;
            moveVector.add(tmp);
        }
        if (keys.containsKey(UP)) {
            jump = true;
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
                if (breakingPos[0] == breakingBlockPos[0] &&
                        breakingPos[1] == breakingBlockPos[1] &&
                        breakingPos[2] == breakingBlockPos[2]) { //the same block is being broken
                    Block block = SpaceQuest.getSpaceQuest().getWorld()
                            .getBlock(breakingPos[0], breakingPos[1],
                                    breakingPos[2]
                            );
                    long passedTime = System.currentTimeMillis() - breakStart;
                    if (passedTime >= block.getHardness() / 0.5f * 1000) {
                        SpaceQuest.getSpaceQuest().getWorld()
                                .breakBlock(breakingPos[0], breakingPos[1], breakingPos[2]);
                        playerInventory.addStack(block.getDrop(breakingPos[0], breakingPos[1],
                                breakingPos[2]
                        ));
                        breakingBlockPos = null;

                    } else { //update break state
                        float breakTime = block.getHardness() / 0.5f * 1000;
                        float stateTime = breakTime / 11;
                        int i = 0;
                        while (passedTime > stateTime) {
                            passedTime -= stateTime;
                            i++;
                        }
                        SpaceQuest.getSpaceQuest().getWorld()
                                .updateBreakState(breakingPos[0], breakingPos[1], breakingPos[2],
                                        i
                                );
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
        this.velocity = velocity / 10;
    }
}
