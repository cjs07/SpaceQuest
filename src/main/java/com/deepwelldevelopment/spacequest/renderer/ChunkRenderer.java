package com.deepwelldevelopment.spacequest.renderer;

import com.deepwelldevelopment.spacequest.block.Block;
import com.deepwelldevelopment.spacequest.util.GLManager;
import com.deepwelldevelopment.spacequest.world.chunk.Chunk;
import com.deepwelldevelopment.spacequest.world.chunk.Layer;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import static com.deepwelldevelopment.spacequest.util.ShaderUtil.createShader;
import static com.deepwelldevelopment.spacequest.util.ShaderUtil.createShaderProgram;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL20.*;

public class ChunkRenderer {

    private FloatBuffer vertices;
    private FloatBuffer uv;

    private int vertexBuffer;
    private int uvBuffer;
    private int textureID;

    private Chunk chunk;

    public ChunkRenderer(Chunk chunk) {
        this.chunk = chunk;

        ArrayList<Float> blockVertices = new ArrayList<>();
        ArrayList<Float> blockUV = new ArrayList<>();

        for(Layer l : chunk.getLayers()) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    if (l != null) {
                        Block b = l.getBlock(x, z);
                        if (b != null) {
                            blockVertices.addAll(b.getDrawnVertices());
                            blockUV.addAll(b.getDrawnUV());
                        }
                    }
                }
            }
        }

        float[] verticesArray = new float[blockVertices.size()];
        for (int i = 0; i < blockVertices.size(); i++) {
            verticesArray[i] = blockVertices.get(i);
        }

        float[] uvArray = new float[blockUV.size()];
        for (int i = 0; i < blockUV.size(); i++) {
            uvArray[i] = blockUV.get(i);
        }
        vertices = FloatBuffer.wrap(verticesArray);
        uv = FloatBuffer.wrap(uvArray);
    }

    public void init() {
        vertices.flip();
        vertexBuffer = GLManager.INSTANCE.getVertexBuffer();
        glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

        uv.flip();
        uvBuffer = GLManager.INSTANCE.getUVBuffer();
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
    }

    //only called when a block in the chunk is added or removed, can probably be optimized
    public void update() {
        new Thread(() -> {

            ArrayList<Float> blockVertices = new ArrayList<>();
            ArrayList<Float> blockUV = new ArrayList<>();

            for (Layer l : chunk.getLayers()) {
                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        if (l != null) {
                            Block b = l.getBlock(x, z);
                            if (b != null) {
                                blockVertices.addAll(b.getDrawnVertices());
                                blockUV.addAll(b.getDrawnUV());
                            }
                        }
                    }
                }
            }

            float[] verticesArray = new float[blockVertices.size()];
            for (int i = 0; i < blockVertices.size(); i++) {
                verticesArray[i] = blockVertices.get(i);
            }

            float[] uvArray = new float[blockUV.size()];
            for (int i = 0; i < blockUV.size(); i++) {
                uvArray[i] = blockUV.get(i);
            }
            vertices = FloatBuffer.wrap(verticesArray);
            uv = FloatBuffer.wrap(uvArray);
        }).start();
    }

    public void render() {
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, ResourceManager.INSTANCE.textureSheet);
        glUniform1i(textureID, 0);

        glEnableVertexAttribArray(0);
        glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);

        glEnableVertexAttribArray(1);
        glBindBuffer(GL_ARRAY_BUFFER, uvBuffer);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);

        glDrawArrays(GL_TRIANGLES, 0, vertices.capacity());
        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
    }
}
