//package com.deepwelldevelopment.spacequest;
//
//import com.badlogic.gdx.ApplicationListener;
//import com.badlogic.gdx.Gdx;
//import com.badlogic.gdx.graphics.Color;
//import com.badlogic.gdx.graphics.GL20;
//import com.badlogic.gdx.graphics.PerspectiveCamera;
//import com.badlogic.gdx.graphics.Texture;
//import com.badlogic.gdx.graphics.g2d.BitmapFont;
//import com.badlogic.gdx.graphics.g2d.Sprite;
//import com.badlogic.gdx.graphics.g2d.SpriteBatch;
//import com.badlogic.gdx.graphics.g2d.TextureAtlas;
//import com.badlogic.gdx.graphics.g3d.Environment;
//import com.badlogic.gdx.graphics.g3d.ModelBatch;
//import com.badlogic.gdx.graphics.g3d.ModelInstance;
//import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
//import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
//import com.badlogic.gdx.graphics.glutils.ShaderProgram;
//import com.badlogic.gdx.scenes.scene2d.Stage;
//import com.badlogic.gdx.scenes.scene2d.ui.Image;
//import com.badlogic.gdx.scenes.scene2d.ui.Label;
//import com.deepwelldevelopment.spacequest.block.BlockModel;
//import com.deepwelldevelopment.spacequest.block.BlockProvider;
//import com.deepwelldevelopment.spacequest.block.IBlockProvider;
//import com.deepwelldevelopment.spacequest.client.render.VoxelRender;
//import com.deepwelldevelopment.spacequest.world.World;
//import com.deepwelldevelopment.spacequest.world.biome.IBiomeProvider;
//import com.deepwelldevelopment.spacequest.world.biome.OverworldBiomeProvider;
//import com.deepwelldevelopment.spacequest.world.chunk.IChunkProvider;
//import com.deepwelldevelopment.spacequest.world.chunk.OverworldChunkProvider;
//
//public class SpaceQuest implements ApplicationListener {
//
//    public static final int MAX_UPDATE_ITERATIONS = 20;
//
//    public static SpaceQuest INSTANCE;
//
//    private Environment environment;
//    private PerspectiveCamera cam;
//    private ModelBatch modelBatch;
//    private BlockModel grassModel;
//    private Texture dirtTexture;
//    private Texture grassTopTexture;
//    private Texture grassSideTexture;
//    private World world;
//    private CameraController camController;
//
//    private Stage stage;
//    private Label label;
//    private BitmapFont font;
//    private Image crosshair;
//    private Texture crosshairTexture;
//    private ModelInstance block;
//
//    private TextureAtlas textureAtlas;
//    private IBlockProvider blockProvider;
//    private IChunkProvider chunkProvider;
//    private IBiomeProvider biomeProvider;
//    private VoxelRender voxelRender;
//    private SpriteBatch spriteBatch;
//    private Sprite crosshairSprite;
//
//    private float accum;
//    private int iterations = 0;
//    private VoxelRender alphaRender;
//    private ModelInstance skypbox;
//    private ModelBatch skyboxRender;
//    private ShaderProgram shaderProgram;
//
//    private Color waterFog = new Color(11/255f,14/255f,41/255f,1);
//    private Color skyFog = new Color(0,0,0,1);
//    private ColorAttribute fogColorAttribute = new ColorAttribute(ColorAttribute.Fog, waterFog);
//    private ColorAttribute skyFogColorAttribute = new ColorAttribute(ColorAttribute.Fog, skyFog);
//
//    private int previousChunks;
//
//    public TextureAtlas getTextureAtlas() {
//        return textureAtlas;
//    }
//
//    @Override
//    public void create() {
//        environment = new Environment();
//        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
//        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));
//        modelBatch = new ModelBatch();
//        cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
//        cam.position.set(10f, 10f, 10f);
//        cam.lookAt(0, 0, 0);
//        cam.near = 1f;
//        cam.far = 300f;
//        cam.update();
//
//        dirtTexture = new Texture("dirt.png");
//        grassTopTexture = new Texture("grass_top.png");
//        grassSideTexture = new Texture("grass_side.png");
//
//        grassModel = new BlockModel("grass", grassSideTexture, grassSideTexture, grassTopTexture, dirtTexture, grassSideTexture, grassSideTexture);
//        new BlockModel("dirt", dirtTexture);
//
//        camController = new CameraController(cam);
//        cam.up.set(0, 1, 0);
//        Gdx.input.setInputProcessor(camController);
//        Gdx.input.setCursorCatched(true);
//
//        stage = new Stage();
//        font = new BitmapFont();
//        label = new Label(" ", new Label.LabelStyle(font, Color.WHITE));
//        crosshairTexture = new Texture("crosshair.png");
//        crosshair = new Image(crosshairTexture);
//        crosshair.setPosition(Gdx.graphics.getWidth() / 2,Gdx.graphics.getHeight() / 2);
//        stage.addActor(label);
//        stage.addActor(crosshair);
//
//        //world stuff
//        textureAtlas = new TextureAtlas(Gdx.files.internal("blocks.atlas"));
//
//        blockProvider = new BlockProvider();
//        biomeProvider = new OverworldBiomeProvider();
//        chunkProvider = new OverworldChunkProvider(blockProvider, biomeProvider);
//        world = new World(blockProvider, chunkProvider, biomeProvider);
//    }
//
//    @Override
//    public void resize(int width, int height) {
//        stage.getViewport().update(width, height, true);
//        cam.viewportWidth = width;
//        cam.viewportHeight = height;
//    }
//
//    @Override
//    public void render() {
//        camController.update();
//
//        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
//        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
//
//        modelBatch.begin(cam);
////        for (ModelInstance instance : blockInstances) {
////            modelBatch.render(instance, environment);
////        }
////        modelBatch.render(worldModel, environment);
//        modelBatch.render(block, environment);
//        modelBatch.end();
//
//        label.setText("FPS: " + Gdx.graphics.getFramesPerSecond());
//        stage.draw();
//    }
//
//    @Override
//    public void pause() {
//    }
//
//    @Override
//    public void resume() {
//    }
//
//    @Override
//    public void dispose() {
//        dirtTexture.dispose();
//        grassTopTexture.dispose();
//        grassSideTexture.dispose();
//        grassModel.dispose();
////        for (BlockInstance bi : blockInstances) {
////            bi.dispose();
////        }
////        blockInstances.clear();
////        worldModel.dispose();
//        modelBatch.dispose();
//        font.dispose();
//        crosshairTexture.dispose();
//        stage.dispose();
//    }
//}

package com.deepwelldevelopment.spacequest;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectSet;
import com.deepwelldevelopment.spacequest.block.BlockProvider;
import com.deepwelldevelopment.spacequest.world.biome.OverworldBiomeProvider;
import com.deepwelldevelopment.spacequest.world.chunk.OverworldChunkProvider;

import java.nio.FloatBuffer;

public class SpaceQuest implements ApplicationListener {

    private PerspectiveCamera camera;
    private VoxelEngine voxelEngine;
    private TextureAtlas textureAtlas;

    private void clearOpenGL() {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        //Render sky color different depending on where the camera is facing.
        Vector3 direction = camera.direction;
        float v = Math.abs(MathUtils.atan2(direction.x, direction.z) * MathUtils.radiansToDegrees);
        v = Math.min(90, v / 2);

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        Gdx.gl.glClearColor((v / 2) / 255f, (v / 1) / 255f, ((255 - v) - 50) / 255f, 1);
        voxelEngine.setSkyColor((v / 2) / 255f, (v / 1) / 255f, ((255 - v) - 50) / 255f, 1);
        //Gdx.gl.glClearColor(4f/255f,4f/255f,20f/255f,1);
    }

    @Override
    public void create() {

    }

    @Override
    public void resize(int width, int height) {
        createCamera(width, height);
        setup();
    }

    @Override
    public void render() {
        clearOpenGL();
        voxelEngine.render(Gdx.graphics.getDeltaTime());
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void dispose() {
        voxelEngine.dispose();
    }

    private void setup() {
        BlockProvider blockProvider = new BlockProvider();
        OverworldBiomeProvider biomeProvider = new OverworldBiomeProvider();
        OverworldChunkProvider chunkProvider = new OverworldChunkProvider(blockProvider, biomeProvider);
        //texture = new Texture(Gdx.files.internal("data/textures.png"), true);
        // texture.setFilter(Texture.TextureFilter.MipMapNearestNearest, Texture.TextureFilter.Nearest);

        textureAtlas = new TextureAtlas(Gdx.files.internal("blocks.atlas"));

        voxelEngine = new VoxelEngine(camera, blockProvider, chunkProvider, biomeProvider, textureAtlas);
        enableAnisotropy();
    }

    private void enableAnisotropy() {
        FloatBuffer buffer = BufferUtils.newFloatBuffer(64);
        if (Gdx.gl20 != null) {
            Gdx.gl20.glGetFloatv(GL20.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT, buffer);
        } else {
            throw new GdxRuntimeException("GL20 not available");
        }

        float maxAnisotropy = buffer.get(0);

        ObjectSet<Texture> textures = textureAtlas.getTextures();
        for (Texture tex : textures) {
            tex.bind();
            //Gdx.gl.glTexParameterf(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_MAX_ANISOTROPY_EXT, Math.min(16, maxAnisotropy));
            Gdx.gl.glTexParameteri(GL20.GL_TEXTURE_2D, GL30.GL_TEXTURE_MAX_LEVEL, 4);
        }
    }

    private void createCamera(int width, int height) {
        camera = new PerspectiveCamera(70f, width, height);
        camera.near = 0.1f;
        camera.far = 200;
        camera.position.set(0, 140, 0);
        camera.lookAt(0, 140, 1);
        camera.rotate(camera.up, 182);
        camera.update();
    }
}
