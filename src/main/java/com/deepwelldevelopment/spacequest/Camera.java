package com.deepwelldevelopment.spacequest;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import static java.lang.Math.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;

public class Camera {

    private long window;

    private float sensitivity = 0.05f;
    private float lastX = 400;
    private float lastY = 300;
    private float pitch;
    private float yaw = -90.0f;

    private Vector3f position;
    private Vector3f front;
    private Vector3f up;

    private Matrix4f view;
    private Matrix4f projection;

    public Camera(long window, float fovDegrees, float width, float height) {
        this.window = window;
        projection =
                new Matrix4f().identity().perspective(
                        (float) toRadians(fovDegrees),
                        width / height, 0.1f, 100.0f
                );
        position = new Vector3f(0.0f, 0.0f, 3.0f);
        front = new Vector3f(0.0f, 0.0f, -1.0f);
        up = new Vector3f(0.0f, 1.0f, 0.0f);
        view = new Matrix4f().lookAt(position,
                new Vector3f(position).add(front), up
        );
    }

    public void update(float deltaTime) {
        float cameraSpeed = 7.5f * deltaTime;
        Vector3f temp = new Vector3f();
        if (glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS) {
            temp.set(front.x, 0, front.z).normalize().mul(cameraSpeed);
            position.add(temp);
        }
        if (glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS) {
            temp.set(front.x, 0, front.z).normalize().mul(cameraSpeed);
            position.sub(temp);
        }
        if (glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS) {
            position.sub(
                    new Vector3f(front).cross(up).normalize()
                            .mul(cameraSpeed));
        }
        if (glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS) {
            position.add(new Vector3f(front).cross(up).normalize()
                    .mul(cameraSpeed));
        }
        view.identity().lookAt(position, new Vector3f(position).add(front), up);
    }

    public void mouseMoved(float x, float y) {
        float xOffset = (float) (x - lastX);
        float yOffset = (float) (lastY - y);
        lastX = (float) x;
        lastY = (float) y;
        xOffset *= sensitivity;
        yOffset *= sensitivity;

        yaw += xOffset;
        pitch += yOffset;

        if (pitch > 89.0f) pitch = 89.0f;
        if (pitch < -89.0f) pitch = -89.0f;

        front.set(
                (float) (cos(toRadians(yaw)) * cos(toRadians(pitch))),
                (float) (sin(toRadians(pitch))),
                (float) (sin(toRadians(yaw)) * cos(toRadians(pitch)))
        ).normalize();
    }

    public Matrix4f getView() {
        return view;
    }

    public Matrix4f getProjection() {
        return projection;
    }
}
