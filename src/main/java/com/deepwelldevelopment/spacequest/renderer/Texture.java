package com.deepwelldevelopment.spacequest.renderer;

import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

import static com.deepwelldevelopment.spacequest.renderer.ResourceManager.TEXTURE_HEIGHT_FLOAT;
import static com.deepwelldevelopment.spacequest.renderer.ResourceManager.TEXTURE_WIDTH_FLOAT;

public class Texture {

    private String name;

    private FloatBuffer uvCoordinates;

    public Texture(String name, float u, float v) {
        this.name = name;
        uvCoordinates = BufferUtils.createFloatBuffer(12);

        System.out.println(name + ": " + u + ", " + v + "\nwidth: " + TEXTURE_WIDTH_FLOAT +"\nheight: " + TEXTURE_HEIGHT_FLOAT + "\n");

        uvCoordinates.put(u).put(v);
        uvCoordinates.put(u+TEXTURE_WIDTH_FLOAT).put(v);
        uvCoordinates.put(u+TEXTURE_WIDTH_FLOAT).put(v+TEXTURE_HEIGHT_FLOAT);
        uvCoordinates.put(u+TEXTURE_WIDTH_FLOAT).put(v+TEXTURE_HEIGHT_FLOAT);
        uvCoordinates.put(u).put(v+TEXTURE_HEIGHT_FLOAT);
        uvCoordinates.put(u).put(v);
        uvCoordinates.rewind();
    }

    public String getName() {
        return name;
    }

    public FloatBuffer getUvCoordinates() {
        return uvCoordinates;
    }

    @Override
    public String toString() {
        return name;
    }
}
