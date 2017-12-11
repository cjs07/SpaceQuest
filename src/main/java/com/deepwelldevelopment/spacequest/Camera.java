package com.deepwelldevelopment.spacequest;

import org.joml.Vector2i;
import org.joml.Vector3f;

import static java.lang.Math.asin;
import static java.lang.Math.toDegrees;
import static org.lwjgl.glfw.GLFW.*;

public class Camera {

    public static final float STEP_SCALE = 1.0f;
    public static final float EDGE_STEP = 0.5f;
    public static final int MARGIN = 10;

    long window;
    int windowWidth;
    int windowHeight;

    Vector3f pos;
    Vector3f target;
    Vector3f up;

    float hAngle;
    float vAngle;

    boolean onUpperEdge;
    boolean onLowerEdge;
    boolean onLeftEdge;
    boolean onRightEdge;

    Vector2i mousePos;

    public Camera(long window, int windowWidth, int windowHeight) {
        this.window = window;
        this.windowWidth = windowWidth;
        this.windowHeight = windowHeight;
        this.pos = new Vector3f(0.0f, 0.0f, 0.0f);
        target = new Vector3f(0.0f, 0.0f, 0.0f);
        target.normalize();
        up = new Vector3f(0.0f, 0.0f, 0.0f);

        mousePos = new Vector2i(0, 0);

        init();
    }

    public Camera(long window, int windowWidth, int windowHeight, Vector3f pos, Vector3f target, Vector3f up) {
        this.window = window;
        this.windowWidth = windowWidth;
        this.windowHeight = windowHeight;
        this.pos = pos;

        this.target = target;
        target.normalize();

        this.up = up;
        up.normalize();

        mousePos = new Vector2i(0, 0);

        init();
    }

    void init() {
        Vector3f hTarget = new Vector3f(target.x, 0.0f, target.z);
        hTarget.normalize();

        if (hTarget.z >= 0.0f) {
            if (hTarget.x >= 0.0f) {
                hAngle = (float) (360.0f - toDegrees(asin(hTarget.z)));
            } else {
                hAngle = (float) (180.0f + toDegrees(asin(hTarget.z)));
            }
        } else {
            if (hTarget.x >= 0.0f) {
                hAngle = (float) toDegrees(-asin(hTarget.z));
            } else {
                hAngle = (float) (180.0f - toDegrees(-asin(hTarget.z)));
            }
        }

        vAngle = (float) toDegrees(asin(target.y));

        onUpperEdge = false;
        onLowerEdge = false;
        onLeftEdge = false;
        onRightEdge = false;
        mousePos.x = windowWidth / 2;
        mousePos.y = windowHeight / 2;
//        glfwSetCursorPos(window, windowWidth / 2, windowHeight / 2);
    }

    boolean onKeyboard(int key) {
        boolean ret = false;
        switch (key) {
            case GLFW_KEY_W:
                pos.add(target.mul(STEP_SCALE));
                ret = true;
                break;
            case GLFW_KEY_S:
                pos.sub(target.mul(STEP_SCALE));
                ret = true;
                break;
            case GLFW_KEY_A:
                Vector3f left = target.cross(up);
                left.normalize();
                left.mul(STEP_SCALE);
                pos.add(left);
                ret = true;
                break;
            case GLFW_KEY_D:
                Vector3f right = target.cross(target);
                right.normalize();
                right.mul(STEP_SCALE);
                pos.add(right);
                ret = true;
                break;
        }
        return ret;
    }

    void onMouse(int x, int y) {
        int deltaX = x - mousePos.x;
        int deltaY = y - mousePos.y;

        mousePos.x = x;
        mousePos.y = y;

        hAngle += (float) deltaX / 20.0f;
        vAngle += (float) deltaY / 20.0f;

        if (deltaX == 0) {
            if (x <= MARGIN) {
//                hAngle -= 1.0f;
                onLeftEdge = true;
            } else if (x >= (windowWidth - MARGIN)) {
//                hAngle += 1.0f;
                onRightEdge = true;
            }
        } else {
            onLeftEdge = false;
            onRightEdge = false;
        }

        if (deltaY == 0) {
            if (y <= MARGIN) {
                onUpperEdge = true;
            }
            else if (y >= (windowHeight - MARGIN)) {
                onLowerEdge = true;
            }
        }
        else {
            onUpperEdge = false;
            onLowerEdge = false;
        }

        update();
    }

    void onRender() {
        boolean shouldUpdate = true;

        if (onLeftEdge) {
            hAngle -= EDGE_STEP;
            shouldUpdate = true;
        }
        else if (onRightEdge) {
            hAngle+= EDGE_STEP;
            shouldUpdate = true;
        }

        if (onUpperEdge) {
            if (vAngle > -90.0f) {
                vAngle -= EDGE_STEP;
                shouldUpdate = true;
            }
        }
        else if (onLowerEdge) {
            if (vAngle < 90.0f) {
                vAngle += EDGE_STEP;
                shouldUpdate = true;
            }
        }

        if (shouldUpdate) {
            update();
        }
    }

    void update() {
        Vector3f vAxis = new Vector3f(0.0f, 1.0f, 0.0f);

        // Rotate the view vector by the horizontal angle around the vertical axis
        Vector3f view = new Vector3f(1.0f, 0.0f, 0.0f);
        view.rotateY(hAngle);
        view.normalize();

        // Rotate the view vector by the vertical angle around the horizontal axis
        Vector3f hAxis = vAxis.cross(view);
        hAxis.normalize();
        view.rotateX(vAngle);

        target = view;
        target.normalize();

        up = target.cross(hAxis);
        up.normalize();
    }


    public Vector3f getPos() {
        return pos;
    }

    public Vector3f getTarget() {
        return target;
    }

    public Vector3f getUp() {
        return up;
    }
}
