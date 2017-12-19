package com.deepwelldevelopment.spacequest.util;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import static org.lwjgl.opengl.GL15.glGenBuffers;

public class GLManager {

    public static GLManager INSTANCE;

    private Queue<Integer> vertexBuffers;
    private Queue<Integer> uvBuffers;

    public GLManager(int maxBuffers) {
        INSTANCE = this;

        vertexBuffers = new ArrayBlockingQueue<>(maxBuffers);
        uvBuffers = new ArrayBlockingQueue<>(maxBuffers);

        for (int i = 0; i < maxBuffers; i++) {
            vertexBuffers.add(glGenBuffers());
            uvBuffers.add(glGenBuffers());
        }
    }

    public int getVertexBuffer() {
        return vertexBuffers.remove();
    }

    public int getUVBuffer() {
        return uvBuffers.remove();
    }
}
