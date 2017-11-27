package com.deepwelldevelopment.spacequest;


import org.lwjgl.BufferUtils;

import java.io.IOException;
import java.nio.FloatBuffer;

import static com.deepwelldevelopment.spacequest.util.ShaderUtil.createShader;
import static com.deepwelldevelopment.spacequest.util.ShaderUtil.createShaderProgram;
import static com.deepwelldevelopment.spacequest.util.ShaderUtil.loadTexture;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;

/***
 * Represents a block object in the world. Each block is self contained, and self-rendering.
 */
public class Block implements ICrossThreadObject {

    enum EnumBlockSide {
        BOTTOM(0),
        TOP(1),
        FRONT(2),
        BACK(3),
        LEFT(4),
        RIGHT(5);

        int id;

        EnumBlockSide(int id) {
            this.id = id;
        }
    }

    private int x;
    private int y;
    private int z;

    private boolean individualTextures;
    private boolean initialized = false;

    private FloatBuffer[] vertices = new FloatBuffer[6];
    private FloatBuffer allVertices;
    private FloatBuffer uv;

    private int[] vertexBuffers = new int[6];
    private int allVertexBuffer;
    private int uvBuffer;

    private int[] textures = new int[6];
    private int textureID;

    private boolean[] toDraw = new boolean[6];

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

        for (int i = 0; i < vertices.length; i++) {
            vertices[i] = BufferUtils.createFloatBuffer(2 * (3 * 3)); //each vertex has three points, each triangle has three vertices and each face has two triangles
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

        for (FloatBuffer vertice : vertices) {
            vertice.flip();
        }

        allVertices = BufferUtils.createFloatBuffer(6 * (2 * (3 * 3))); //each vertex has three points, each triangle has three vertices, each face has two triangle, and each block has 5 faces

        for (FloatBuffer fb : vertices) {
            allVertices.put(fb);
        }
        allVertices.flip();

        uv = BufferUtils.createFloatBuffer(6 * (2 * (3 * 2)));
        uv.put(0.0f).put(0.0f);
        uv.put(1.0f).put(0.0f);
        uv.put(1.0f).put(1.0f);
        uv.put(1.0f).put(1.0f);
        uv.put(0.0f).put(1.0f);
        uv.put(0.0f).put(0.0f);
        uv.put(0.0f).put(0.0f);
        uv.put(1.0f).put(0.0f);
        uv.put(1.0f).put(1.0f);
        uv.put(1.0f).put(1.0f);
        uv.put(0.0f).put(1.0f);
        uv.put(0.0f).put(0.0f);
        uv.put(0.0f).put(0.0f);
        uv.put(1.0f).put(0.0f);
        uv.put(1.0f).put(1.0f);
        uv.put(1.0f).put(1.0f);
        uv.put(0.0f).put(1.0f);
        uv.put(0.0f).put(0.0f);
        uv.put(0.0f).put(0.0f);
        uv.put(1.0f).put(0.0f);
        uv.put(1.0f).put(1.0f);
        uv.put(1.0f).put(1.0f);
        uv.put(0.0f).put(1.0f);
        uv.put(0.0f).put(0.0f);
        uv.put(0.0f).put(0.0f);
        uv.put(1.0f).put(0.0f);
        uv.put(1.0f).put(1.0f);
        uv.put(1.0f).put(1.0f);
        uv.put(0.0f).put(1.0f);
        uv.put(0.0f).put(0.0f);
        uv.put(0.0f).put(0.0f);
        uv.put(1.0f).put(0.0f);
        uv.put(1.0f).put(1.0f);
        uv.put(1.0f).put(1.0f);
        uv.put(0.0f).put(1.0f);
        uv.put(0.0f).put(0.0f);
        uv.flip();

        for (int i = 0; i < vertices.length; i++) {
            vertexBuffers[i] = glGenBuffers();
            glBindBuffer(GL_ARRAY_BUFFER, vertexBuffers[i]);
            glBufferData(GL_ARRAY_BUFFER, vertices[i], GL_STATIC_DRAW);
        }

        allVertexBuffer = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, allVertexBuffer);
        glBufferData(GL_ARRAY_BUFFER, allVertices, GL_STATIC_DRAW);

        uvBuffer = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, uvBuffer);
        glBufferData(GL_ARRAY_BUFFER, uv, GL_STATIC_DRAW);

        try {
            int vShader = createShader("vertex.vert", GL_VERTEX_SHADER);
            int fShader = createShader("fragment.frag", GL_FRAGMENT_SHADER);
            int program = createShaderProgram(vShader, fShader);
            textureID = glGetUniformLocation(program, "textureSampler");
        } catch (IOException e) {
            e.printStackTrace();
        }
        initialized = true;

//        if (Thread.currentThread().getName().equals("renderThread")) {
//            init();
//        } else {
//            ThreadManager.INSTANCE.openRequest(new CrossThreadRequest(ThreadManager.GENERATION_THREAD, ThreadManager.RENDER_THREAD, CrossThreadRequest.BLOCK_INIT_REQUEST, this));
//            initialized = false;
//        }
    }

    @Override
    public void completeRequest() {
        if (!initialized) {
            init();
        }
    }

    public void init() {
        for (int i = 0; i < vertices.length; i++) {
            vertexBuffers[i] = glGenBuffers();
            glBindBuffer(GL_ARRAY_BUFFER, vertexBuffers[i]);
            glBufferData(GL_ARRAY_BUFFER, vertices[i], GL_STATIC_DRAW);
        }

        allVertexBuffer = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, allVertexBuffer);
        glBufferData(GL_ARRAY_BUFFER, allVertices, GL_STATIC_DRAW);

        uvBuffer = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, uvBuffer);
        glBufferData(GL_ARRAY_BUFFER, uv, GL_STATIC_DRAW);

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

    public void setSidedTexture(String texture, int side) {
        try {
            individualTextures = true;
            textures[side] = loadTexture(texture);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setToDraw(boolean toDraw, int side) {
        this.toDraw[side] = toDraw;
    }

    /**
     * Draws this block. Each block is responsible for drawing itself.
     */
    public void draw() {
        if (!initialized) {
            return;
        }
        if (individualTextures) {
            for (EnumBlockSide side : EnumBlockSide.values()) {
                if (toDraw[side.ordinal()]) {
                    glActiveTexture(GL_TEXTURE0);
                    glBindTexture(GL_TEXTURE_2D, textures[side.ordinal()]);
                    glUniform1i(textureID, 0);

                    glEnableVertexAttribArray(0);
                    glBindBuffer(GL_ARRAY_BUFFER, vertexBuffers[side.ordinal()]);
                    glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);

                    glEnableVertexAttribArray(1);
                    glBindBuffer(GL_ARRAY_BUFFER, uvBuffer);
                    glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);

                    glDrawArrays(GL_TRIANGLES, 0, vertices[side.ordinal()].capacity());
                    glDisableVertexAttribArray(0);
                    glDisableVertexAttribArray(1);
                }
            }
        } else { //dont load nonexistent textures
            for (EnumBlockSide side : EnumBlockSide.values()) {
                if (toDraw[side.ordinal()]) {
                    glActiveTexture(GL_TEXTURE0);
                    glBindTexture(GL_TEXTURE_2D, textures[0]);
                    glUniform1i(textureID, 0);

                    glEnableVertexAttribArray(0);
                    glBindBuffer(GL_ARRAY_BUFFER, vertexBuffers[side.ordinal()]);
                    glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);

                    glEnableVertexAttribArray(1);
                    glBindBuffer(GL_ARRAY_BUFFER, uvBuffer);
                    glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);

                    glDrawArrays(GL_TRIANGLES, 0, vertices[side.ordinal()].capacity());
                    glDisableVertexAttribArray(0);
                    glDisableVertexAttribArray(1);
                }
            }
        }
    }

    /**
     * Releases memory from this block
     */
    public void cleanup() {
        glDeleteBuffers(vertexBuffers);
        glDeleteBuffers(allVertexBuffer);
        glDeleteBuffers(uvBuffer);
        glDeleteTextures(textures);
    }
}
