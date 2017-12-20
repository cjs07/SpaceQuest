package com.deepwelldevelopment.spacequest.block;


import com.deepwelldevelopment.spacequest.CrossThreadRequest;
import com.deepwelldevelopment.spacequest.ICrossThreadObject;
import com.deepwelldevelopment.spacequest.ThreadManager;
import com.deepwelldevelopment.spacequest.renderer.ResourceManager;
import com.deepwelldevelopment.spacequest.renderer.Texture;
import org.lwjgl.BufferUtils;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import static com.deepwelldevelopment.spacequest.util.ShaderUtil.createShader;
import static com.deepwelldevelopment.spacequest.util.ShaderUtil.createShaderProgram;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;

/***
 * Represents a block object in the world. Each block is self contained, and self-rendering.
 */
public class Block implements ICrossThreadObject {

    public enum EnumBlockSide {
        BOTTOM(0, "bottom"),
        TOP(1, "top"),
        FRONT(2, "front"),
        BACK(3, "back"),
        LEFT(4, "left"),
        RIGHT(5, "right");

        int id;
        String name;

        EnumBlockSide(int id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    public int x;
    public int y;
    public int z;

    private boolean individualTextures;

    private FloatBuffer[] vertices = new FloatBuffer[6];
    private FloatBuffer[] uv = new FloatBuffer[6];

    private int[] vertexBuffers = new int[6];
    private int[] uvBuffers = new int[6];

    private Texture[] textures = new Texture[6];
    private int textureID;

    private boolean[] toDraw = new boolean[6];

    private boolean initialized;

    public Block(int x, int y, int z) {
        this(x, y, z, false);
    }

    /***
     * Block constructor
     * @param x x-position of the block
     * @param y y-position of the block
     * @param z z-position of the block
     * @param individualTextures true if each side of the block should be rendered individually (i.e. for individually textured sides)
     */
    public Block(int x, int y, int z, boolean individualTextures) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.individualTextures = individualTextures;

        boolean toInit = Thread.currentThread().getName().equals(ThreadManager.INSTANCE.getRenderThread().getName());

        for (int i = 0; i < vertices.length; i++) {
            vertices[i] = BufferUtils.createFloatBuffer(2 * (3 * 3)); //each vertex has three points, each triangle has three vertices and each face has two triangles
            uv[i] = BufferUtils.createFloatBuffer(2 * (3 * 2));
            toDraw[i] = true;
        }

        vertices[0].put(x).put(y).put(z - 1);
        vertices[0].put(x + 1).put(y).put(z - 1);
        vertices[0].put(x + 1).put(y).put(z);
        vertices[0].put(x + 1).put(y).put(z);
        vertices[0].put(x).put(y).put(z);
        vertices[0].put(x).put(y).put(z - 1);

        vertices[1].put(x).put(y + 1).put(z - 1);
        vertices[1].put(x + 1).put(y + 1).put(z - 1);
        vertices[1].put(x + 1).put(y + 1).put(z);
        vertices[1].put(x + 1).put(y + 1).put(z);
        vertices[1].put(x).put(y + 1).put(z);
        vertices[1].put(x).put(y + 1).put(z - 1);

        vertices[2].put(x).put(y).put(z);
        vertices[2].put(x + 1).put(y).put(z);
        vertices[2].put(x + 1).put(y + 1).put(z);
        vertices[2].put(x + 1).put(y + 1).put(z);
        vertices[2].put(x).put(y + 1).put(z);
        vertices[2].put(x).put(y).put(z);

        vertices[3].put(x + 1).put(y).put(z - 1);
        vertices[3].put(x + 1).put(y + 1).put(z - 1);
        vertices[3].put(x).put(y + 1).put(z - 1);
        vertices[3].put(x).put(y + 1).put(z - 1);
        vertices[3].put(x).put(y).put(z - 1);
        vertices[3].put(x + 1).put(y).put(z - 1);

        vertices[4].put(x).put(y).put(z);
        vertices[4].put(x).put(y).put(z - 1);
        vertices[4].put(x).put(y + 1).put(z - 1);
        vertices[4].put(x).put(y + 1).put(z - 1);
        vertices[4].put(x).put(y + 1).put(z);
        vertices[4].put(x).put(y).put(z);

        vertices[5].put(x + 1).put(y).put(z);
        vertices[5].put(x + 1).put(y).put(z - 1);
        vertices[5].put(x + 1).put(y + 1).put(z - 1);
        vertices[5].put(x + 1).put(y + 1).put(z - 1);
        vertices[5].put(x + 1).put(y + 1).put(z);
        vertices[5].put(x + 1).put(y).put(z);

        //TODO: REPLACE WITH NONEXISTENT TEXTURE
        for (EnumBlockSide side : EnumBlockSide.values()) {
            textures[side.ordinal()] = ResourceManager.INSTANCE.textures.get("dirt");
        }

        if (toInit) {
            for (int i = 0; i < vertices.length; i++) {
                vertices[i].flip();

                vertexBuffers[i] = glGenBuffers();
                glBindBuffer(GL_ARRAY_BUFFER, vertexBuffers[i]);
                glBufferData(GL_ARRAY_BUFFER, vertices[i], GL_STATIC_DRAW);

                FloatBuffer buf = textures[i].getUvCoordinates();
                uv[i].put(buf);
                buf.rewind();

                uv[i].flip();
                uvBuffers[i] = glGenBuffers();
                glBindBuffer(GL_ARRAY_BUFFER, uvBuffers[i]);
                glBufferData(GL_ARRAY_BUFFER, uv[i], GL_DYNAMIC_DRAW);
            }

            try {
                int vShader = createShader("vertex.vert", GL_VERTEX_SHADER);
                int fShader = createShader("fragment.frag", GL_FRAGMENT_SHADER);
                int program = createShaderProgram(vShader, fShader);
                textureID = glGetUniformLocation(program, "textureSampler");
            } catch (IOException e) {
                e.printStackTrace();
            }
            initialized = true;
        } else {
            ThreadManager.INSTANCE.openRequest(new CrossThreadRequest(ThreadManager.GENERATION_THREAD, ThreadManager.RENDER_THREAD,
                    CrossThreadRequest.BLOCK_INIT_REQUEST, this));
        }
    }

    @Override
    public void completeRequest() {
        init();
    }

    public void init() {
        if (!initialized) {
            for (int i = 0; i < vertices.length; i++) {
                vertices[i].flip();

                vertexBuffers[i] = glGenBuffers();
                glBindBuffer(GL_ARRAY_BUFFER, vertexBuffers[i]);
                glBufferData(GL_ARRAY_BUFFER, vertices[i], GL_STATIC_DRAW);

                FloatBuffer buf = textures[i].getUvCoordinates();
                uv[i].clear();
                uv[i].put(buf);
                buf.rewind();

                uv[i].flip();
                uvBuffers[i] = glGenBuffers();
                glBindBuffer(GL_ARRAY_BUFFER, uvBuffers[i]);
                glBufferData(GL_ARRAY_BUFFER, uv[i], GL_DYNAMIC_DRAW);
            }

            try {
                int vShader = createShader("vertex.vert", GL_VERTEX_SHADER);
                int fShader = createShader("fragment.frag", GL_FRAGMENT_SHADER);
                int program = createShaderProgram(vShader, fShader);
                textureID = glGetUniformLocation(program, "textureSampler");
            } catch (IOException e) {
                e.printStackTrace();
            }
            initialized = true;
        }
    }

    public void setSidedTexture(String texture, int side) {
        individualTextures = true;
        textures[side] = ResourceManager.INSTANCE.textures.get(texture);
        uv[side].clear();
        FloatBuffer buf = textures[side].getUvCoordinates();
        uv[side].put(buf);
        uv[side].flip();
//        glBindBuffer(GL_ARRAY_BUFFER, uvBuffers[side]);
//        glBufferSubData(GL_ARRAY_BUFFER, 0, uv[side]);
        buf.rewind();
    }

    public void setToDraw(boolean toDraw, int side) {
        this.toDraw[side] = toDraw;
    }

    public ArrayList<Float> getDrawnVertices() {
        ArrayList<Float> ret = new ArrayList<>();
        for (EnumBlockSide side : EnumBlockSide.values()) {
            if (toDraw[side.ordinal()]) {
                FloatBuffer buf = vertices[side.ordinal()];
                buf.rewind();
                while (buf.hasRemaining()) {
                    ret.add(buf.get());
                }
            }
        }
        return ret;
    }

    public ArrayList<Float> getDrawnUV() {
        ArrayList<Float> ret = new ArrayList<>();
        for (EnumBlockSide side : EnumBlockSide.values()) {
            if (toDraw[side.ordinal()]) {
                FloatBuffer buf = uv[side.ordinal()];
                buf.rewind();
                while (buf.hasRemaining()) {
                    ret.add(buf.get());
                }
            }
        }
        return ret;
    }

    /**
     * Draws this block. Each block is responsible for drawing itself.
     */
    public void draw() {
        if (!initialized) {
            return;
        }
        for (EnumBlockSide side : EnumBlockSide.values()) {
            if (toDraw[side.ordinal()]) {
                glActiveTexture(GL_TEXTURE0);
                glBindTexture(GL_TEXTURE_2D, ResourceManager.INSTANCE.textureSheet);
                glUniform1i(textureID, 0);

                glEnableVertexAttribArray(0);
                glBindBuffer(GL_ARRAY_BUFFER, vertexBuffers[side.ordinal()]);
                glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);

                glEnableVertexAttribArray(1);
                glBindBuffer(GL_ARRAY_BUFFER, uvBuffers[side.ordinal()]);
                glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);

                glDrawArrays(GL_TRIANGLES, 0, vertices[side.ordinal()].capacity());
                glDisableVertexAttribArray(0);
                glDisableVertexAttribArray(1);
            }
        }
    }

    /**
     * Releases memory from this block
     */
    public void cleanup() {
        glDeleteBuffers(vertexBuffers);
        glDeleteBuffers(uvBuffers);
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder("Position: (" + x + ", " + y + ", " + z + ") \n");
        str.append("Texture ID: ").append(textureID).append("\n");
        str.append("Individual textures: ").append(individualTextures).append("\n");
        str.append("Sided Data:\n");
        for (EnumBlockSide side : EnumBlockSide.values()) {
            str.append("\tSide: ").append(side).append("\n");
            str.append("\ttoDraw: ").append(toDraw[side.ordinal()]).append("\n");
            str.append("\tVertex Buffer: ").append(vertexBuffers[side.ordinal()]).append("\n");
            str.append("UV Buffer: ").append(uvBuffers[side.ordinal()]).append("\n");
            str.append("\tTexture: ").append(textures[side.ordinal()]).append("\n");
        }
        return str.toString();
    }
}
