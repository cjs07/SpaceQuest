package com.deepwelldevelopment.spacequest;

import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL.createCapabilities;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import static org.lwjgl.system.MemoryUtil.NULL;

public class SpaceQuest {

    public static void main(String[] args) {
        if (!glfwInit()) {
            System.err.println("Failed to initialize GLFW");
        }

        glfwWindowHint(GLFW_SAMPLES, 4);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

        long window = glfwCreateWindow(1024, 768, "Space Quest", NULL, NULL);
        if (window == NULL) {
            System.err.println("Failed to open Window. Is OpenGL 3.3 supported on your system?");
            return;
        }

        glfwMakeContextCurrent(window);
        glfwShowWindow(window);

        glfwSetInputMode(window, GLFW_STICKY_KEYS, GL_TRUE);

        createCapabilities(); //DON'T EVER FORGET THIS CALL; FUCK YOU C++ FOR NOT NEEDING IT YOU CONFOUNDED ME FOR 20 MINUTES
        glClearColor(0.0f, 0.0f, 0.4f, 0.0f);

        int vao = glGenVertexArrays();
        glBindVertexArray(vao);

        FloatBuffer fb = BufferUtils.createFloatBuffer(3 * 3);
        fb.put(-1.0f).put(-1.0f).put(0.0f);
        fb.put(1.0f).put(-1.0f).put(0.0f);
        fb.put(0.0f).put(1.0f).put(0.0f);
        fb.flip();

        int vertexBuffer = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer);
        glBufferData(GL_ARRAY_BUFFER, fb, GL_STATIC_DRAW);

        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            glEnableVertexAttribArray(0);
            glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer);
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
            glDrawArrays(GL_TRIANGLES, 0, 3);
            glDisableVertexAttribArray(0);

            glfwSwapBuffers(window);
            glfwPollEvents();
        }
        glDeleteBuffers(vertexBuffer);
        glDeleteVertexArrays(vao);
        glfwTerminate();
    }
}
