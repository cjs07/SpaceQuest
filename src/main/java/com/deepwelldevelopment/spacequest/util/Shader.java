package com.deepwelldevelopment.spacequest.util;

import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL20.glDeleteShader;

public class Shader {

    private int id;

    public Shader(String vertexPath, String fragmentPath) {
        ByteBuffer vertexByteBuffer, fragmentByteBuffer;
        try {
            vertexByteBuffer = IOUtils.ioResourceToByteBuffer(vertexPath, 1024);
            fragmentByteBuffer = IOUtils.ioResourceToByteBuffer(fragmentPath,
                    1024
            );

        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        PointerBuffer vertexStrings = BufferUtils.createPointerBuffer(1);
        vertexStrings.put(0, vertexByteBuffer);
        IntBuffer vertexLengths = BufferUtils.createIntBuffer(1);
        vertexLengths.put(0, vertexByteBuffer.remaining());

        IntBuffer intBuffer = BufferUtils.createIntBuffer(1);
        int vertexShader = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertexShader, vertexStrings, vertexLengths);
        glCompileShader(vertexShader);
        glGetShaderiv(vertexShader, GL_COMPILE_STATUS, intBuffer);
        if (intBuffer.get(0) == 0) {
            String info = glGetShaderInfoLog(vertexShader);
            System.err.println("Failed to compile vertex shader: " + info);
            return;
        }

        PointerBuffer fragmentStrings = BufferUtils.createPointerBuffer(1);
        fragmentStrings.put(0, fragmentByteBuffer);
        IntBuffer fragmentLengths = BufferUtils.createIntBuffer(1);
        fragmentLengths.put(0, fragmentByteBuffer.remaining());

        int fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragmentShader, fragmentStrings, fragmentLengths);
        glCompileShader(fragmentShader);
        glGetShaderiv(fragmentShader, GL_COMPILE_STATUS, intBuffer);
        if (intBuffer.get(0) == 0) {
            String info = glGetShaderInfoLog(fragmentShader);
            System.err.println("Failed to compile fragment shader: " + info);
            return;
        }

        id = glCreateProgram();
        glAttachShader(id, vertexShader);
        glAttachShader(id, fragmentShader);
        glLinkProgram(id);
        glGetProgramiv(id, GL_LINK_STATUS, intBuffer);
        if (intBuffer.get(0) == 0) {
            String info = glGetProgramInfoLog(id);
            System.err.println("Failed to link program: " + info);
            return;
        }
        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);
    }

    public void use() {
        glUseProgram(id);
    }

    public void setBool(String name, boolean value) {
        glUniform1i(glGetUniformLocation(id, name), value ? 1 : 0);
    }

    public void setInt(String name, int value) {
        glUniform1i(glGetUniformLocation(id, name), value);
    }

    public void setFloat(String name, float value) {
        glUniform1f(glGetUniformLocation(id, name), value);
    }

    public void setFloat3(String name, float f1, float f2, float f3) {
        glUniform3f(glGetUniformLocation(id, name), f1, f2, f3);
    }

    public void setFloat4(String name, float f0, float f1, float f2, float f3) {
        glUniform4f(glGetUniformLocation(id, name), f0, f1, f2, f3);
    }
}
