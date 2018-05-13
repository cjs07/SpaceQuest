package com.deepwelldevelopment.spacequest.block;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.utils.Disposable;

import java.util.HashMap;
import java.util.Map;

public class BlockModel implements Disposable {

    public static final Map<String, BlockModel> blockModels = new HashMap<>();

    private static int attr = Usage.Position | Usage.Normal | Usage.TextureCoordinates;
    private static MeshPartBuilder meshBuilder;

    private MeshPart front;
    private MeshPart back;
    private MeshPart top;
    private MeshPart bottom;
    private MeshPart left;
    private MeshPart right;

    private Texture frontTexture;
    private Texture backTexture;
    private Texture topTexture;
    private Texture bottomTexture;
    private Texture leftTexture;
    private Texture rightTexture;

    public BlockModel(String name, Texture frontTexture, Texture backTexture, Texture topTexture, Texture bottomTexture,
                      Texture leftTexture, Texture rightTexture) {
        this.frontTexture = frontTexture;
        this.backTexture = backTexture;
        this.topTexture = topTexture;
        this.bottomTexture = bottomTexture;
        this.leftTexture = leftTexture;
        this.rightTexture = rightTexture;

        MeshBuilder meshBuilder = new MeshBuilder();

        meshBuilder.begin(attr);
        this.front = meshBuilder.part("front", GL20.GL_TRIANGLES);
        meshBuilder.rect(-0.5f, -0.5f, -0.5f, -0.5f, 0.5f, -0.5f, 0.5f, 0.5f, -0.5f, 0.5f, -0.5f, -0.5f, 0, 0, -1);
        meshBuilder.end();

        meshBuilder.begin(attr);
        this.back = meshBuilder.part("back", GL20.GL_TRIANGLES);
        meshBuilder.rect(-0.5f, 0.5f, 0.5f, -0.5f, -0.5f, 0.5f, 0.5f, -0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0, 0, 1);
        meshBuilder.end();

        meshBuilder.begin(attr);
        this.top = meshBuilder.part("top", GL20.GL_TRIANGLES);
        meshBuilder.rect(-0.5f, 0.5f, -0.5f, -0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, -0.5f, 0, 1, 0);
        meshBuilder.end();

        meshBuilder.begin(attr);
        this.bottom = meshBuilder.part("bottom", GL20.GL_TRIANGLES);
        meshBuilder.rect(-0.5f, -0.5f, 0.5f, -0.5f, -0.5f, -0.5f, 0.5f, -0.5f, -0.5f, 0.5f, -0.5f, 0.5f, 0, -1, 0);
        meshBuilder.end();

        meshBuilder.begin(attr);
        this.left = meshBuilder.part("left", GL20.GL_TRIANGLES);
        meshBuilder.rect(-0.5f, -0.5f, 0.5f, -0.5f, 0.5f, 0.5f, -0.5f, 0.5f, -0.5f, -0.5f, -0.5f, -0.5f, -1, 0, 0);
        meshBuilder.end();

        meshBuilder.begin(attr);
        this.right = meshBuilder.part("right", GL20.GL_TRIANGLES);
        meshBuilder.rect(0.5f, -0.5f, -0.5f, 0.5f, 0.5f, -0.5f, 0.5f, 0.5f, 0.5f, 0.5f, -0.5f, 0.5f, 1, 0, 0);
        meshBuilder.end();
    }

    /**
     * Simplified constructor for blocks that do not have individual textures for each side
     *
     * @param name    The name of the block that this model represents
     * @param texture The texture that all sides of the model will use
     */
    public BlockModel(String name, Texture texture) {
        this(name, texture, texture, texture, texture, texture, texture);
    }

    /**
     * Creates a model for this block, but only adds the faces as designated by the parameters
     *
     * @param front
     * @param back
     * @param top
     * @param bottom
     * @param left
     * @param right
     * @return
     */
    public Model createModel(boolean front, boolean back, boolean top, boolean bottom, boolean left, boolean right) {
        ModelBuilder modelBuilder = new ModelBuilder();
        modelBuilder.begin();
        if (front) {
            modelBuilder.part(this.front, new Material(TextureAttribute.createDiffuse(frontTexture)));
        }
        if (back) {
            modelBuilder.part(this.back, new Material(TextureAttribute.createDiffuse(backTexture)));
        }
        if (top) {
            modelBuilder.part(this.top, new Material(TextureAttribute.createDiffuse(topTexture)));
        }
        if (bottom) {
            modelBuilder.part(this.bottom, new Material(TextureAttribute.createDiffuse(bottomTexture)));
        }
        if (left) {
            modelBuilder.part(this.left, new Material(TextureAttribute.createDiffuse(leftTexture)));
        }
        if (right) {
            modelBuilder.part(this.right, new Material(TextureAttribute.createDiffuse(rightTexture)));
        }
        return modelBuilder.end();
    }

    /**
     * Creates a model representation of this block. Includes all block faces
     *
     * @return The model of this block, with all faces added
     */
    public Model createModel() {
        ModelBuilder modelBuilder = new ModelBuilder();
        modelBuilder.begin();
        modelBuilder.part(front, new Material(TextureAttribute.createDiffuse(frontTexture)));
        modelBuilder.part(back, new Material(TextureAttribute.createDiffuse(backTexture)));
        modelBuilder.part(top, new Material(TextureAttribute.createDiffuse(topTexture)));
        modelBuilder.part(bottom, new Material(TextureAttribute.createDiffuse(bottomTexture)));
        modelBuilder.part(left, new Material(TextureAttribute.createDiffuse(leftTexture)));
        modelBuilder.part(right, new Material(TextureAttribute.createDiffuse(rightTexture)));
        return modelBuilder.end();
    }

    @Override
    public void dispose() {

    }
}
