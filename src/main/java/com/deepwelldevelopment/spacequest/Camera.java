package com.deepwelldevelopment.spacequest;

import org.joml.Vector2i;
import org.joml.Vector3f;

import static java.lang.Math.asin;
import static java.lang.Math.toDegrees;
import static org.lwjgl.glfw.GLFW.glfwSetCursorPos;

public class Camera {

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

    public Camera(long window, int windowWidth, int windowHeight, Vector3f pos, Vector3f target, Vector3f up) {
        this.window = window;
        this.windowWidth = windowWidth;
        this.windowHeight = windowHeight;
        this.pos = pos;

        this.target = target;
        target.normalize();

        this.up = up;
        up.normalize();

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
        glfwSetCursorPos(window, windowWidth / 2, windowHeight / 2);
    }

    void onMouse(int x, int y) {
        int deltaX = x - mousePos.x;
        int deltaY = y - mousePos.y;

        mousePos.x = x;
        mousePos.y = y;

        hAngle += (float) deltaX / 20.0f;
        vAngle += (float) deltaY / 20.0f;
    }

    void update() {

    }

    void onRender() {

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
