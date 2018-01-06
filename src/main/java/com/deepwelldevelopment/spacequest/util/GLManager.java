package com.deepwelldevelopment.spacequest.util;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import static org.lwjgl.opengl.GL15.glGenBuffers;

public class GLManager {

    public static GLManager INSTANCE;

    private Queue<Integer> vertexBuffers;
    private Queue<Integer> uvBuffers;
    private Queue<Integer> selectionBuffers;

    public GLManager(int maxBuffers) {
        INSTANCE = this;

        vertexBuffers = new ArrayBlockingQueue<>(maxBuffers);
        uvBuffers = new ArrayBlockingQueue<>(maxBuffers);
        selectionBuffers = new ArrayBlockingQueue<>(maxBuffers);

        for (int i = 0; i < maxBuffers; i++) {
            vertexBuffers.add(glGenBuffers());
            uvBuffers.add(glGenBuffers());
            selectionBuffers.add(glGenBuffers());
        }
    }

    public int getVertexBuffer() {
        return vertexBuffers.remove();
    }

    public int getUVBuffer() {
        return uvBuffers.remove();
    }

    public int getSelectionBuffer() {
        return selectionBuffers.remove();
    }
}
