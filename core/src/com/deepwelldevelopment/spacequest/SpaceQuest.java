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
//    public static SpaceQuest spaceQuest;
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
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.*;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectSet;
import com.deepwelldevelopment.spacequest.block.BlockProvider;
import com.deepwelldevelopment.spacequest.block.IBlockProvider;
import com.deepwelldevelopment.spacequest.client.render.VoxelRender;
import com.deepwelldevelopment.spacequest.world.World;
import com.deepwelldevelopment.spacequest.world.biome.IBiomeProvider;
import com.deepwelldevelopment.spacequest.world.biome.OverworldBiomeProvider;
import com.deepwelldevelopment.spacequest.world.chunk.IChunkProvider;

import java.nio.FloatBuffer;

public class SpaceQuest implements ApplicationListener {

    public static final int MAX_UPDATE_ITERATIONS = 20;
    public static final float fixedTimeStep = 1 / 60f;
    private static SpaceQuest spaceQuest;
    private PerspectiveCamera camera;
    private TextureAtlas textureAtlas;
    private CameraController cameraController;
    private Environment environment;
    private ModelBatch voxelBatch;
    private VoxelRender voxelRender;
    private SpriteBatch spriteBatch;
    private BitmapFont font;
    private Texture crosshair;
    private Sprite crosshairSprite;
    private VoxelRender alphaVoxelRender;
    private ModelInstance skybox;
    private ModelBatch skyboxRender;
    private ShaderProgram shaderProgram;

    private Color waterFog = new Color(11 / 255f, 14 / 255f, 41 / 255f, 1);
    private Color skyFog = new Color(0, 0, 0, 1);
    private ColorAttribute fogColorAttribute = new ColorAttribute(ColorAttribute.Fog, waterFog);
    private ColorAttribute skyFogColorAttribute = new ColorAttribute(ColorAttribute.Fog, skyFog);

    private float accum = 0;
    private int iterations = 0;

    private IBlockProvider blockProvider;
    private IChunkProvider chunkProvider;
    private IBiomeProvider biomeProvider;
    private World world;

    public static SpaceQuest getSpaceQuest() {
        return spaceQuest;
    }

    private void clearOpenGL() {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        //Render sky color different depending on where the camera is facing.
        Vector3 direction = camera.direction;
        float v = Math.abs(MathUtils.atan2(direction.x, direction.z) * MathUtils.radiansToDegrees);
        v = Math.min(90, v / 2);

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        Gdx.gl.glClearColor((v / 2) / 255f, (v / 1) / 255f, ((255 - v) - 50) / 255f, 1);
        setSkyColor((v / 2) / 255f, (v / 1) / 255f, ((255 - v) - 50) / 255f, 1);
        //Gdx.gl.glClearColor(4f/255f,4f/255f,20f/255f,1);
    }

    @Override
    public void create() {
        spaceQuest = this;
    }

    @Override
    public void resize(int width, int height) {
        createCamera(width, height);
        setup();
    }

    @Override
    public void render() {
        clearOpenGL();

        cameraController.update();

        accum += Gdx.graphics.getDeltaTime();
        iterations = 0;
        renderModelBatches();
        renderSpriteBatches();
        while (accum > fixedTimeStep && iterations < MAX_UPDATE_ITERATIONS) {
            world.update(camera.position);
            tickPhysics(fixedTimeStep);
            skybox.transform.rotate(Vector3.X, fixedTimeStep / 2).setTranslation(camera.position);
            accum -= fixedTimeStep;
            iterations++;
        }
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void dispose() {
        voxelBatch.dispose();
    }

    private void renderModelBatches() {
        if (!world.isPlayerInWater(camera)) {

//            skyboxRender.begin(camera);
//            skyboxRender.render(skybox);
//            skyboxRender.end();
        }
        renderVoxelBatch();
    }

    private void renderVoxelBatch() {
        if (!world.isPlayerInWater(camera)) {
            shaderProgram.begin();
            shaderProgram.setUniformf("u_fogstr", 0.04f);
            environment.set(skyFogColorAttribute);
        } else {
            shaderProgram.begin();
            shaderProgram.setUniformf("u_fogstr", 0.10f);
            environment.set(fogColorAttribute);
            Gdx.gl.glClearColor(waterFog.r, waterFog.g, waterFog.b, waterFog.a);
        }
        voxelBatch.begin(camera);
        voxelBatch.render(voxelRender, environment);
        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
        voxelBatch.render(alphaVoxelRender, environment);
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        voxelBatch.end();
    }

    private void renderSpriteBatches() {
        spriteBatch.begin();
        font.draw(spriteBatch, "fps: " + Gdx.graphics.getFramesPerSecond() +
                "  -  visible/total chunks: " + VoxelRender.getNumberOfVisibleChunks() +
                "/" + VoxelRender.getNumberOfChunks() + "  -  visible/total blocks: " +
                VoxelRender.getNumberOfVisibleBlocks() + "/" + VoxelRender.getBlockCounter() +
                "  -  visible vertices:" + VoxelRender.getNumberOfVertices() + "  -  visible indicies: " +
                VoxelRender.getNumberOfIndices(), 0, 20);

        crosshairSprite.setPosition(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);
        crosshairSprite.draw(spriteBatch);

        spriteBatch.end();
    }

    private void tickPhysics(float delta) {
//        PhysicsController.update(delta);
        camera.update(true);
    }

    private void setup() {
        this.blockProvider = new BlockProvider();
        this.biomeProvider = new OverworldBiomeProvider();
        this.world = new World(blockProvider, biomeProvider);
        this.chunkProvider = world.getChunkProvider();
        textureAtlas = new TextureAtlas(Gdx.files.internal("blocks.atlas"));

        font = new BitmapFont();
        Material material = setupMaterialAndEnvironment();
        setupRendering(material);

        setupCameraController();

        crosshair = new Texture(Gdx.files.internal("crosshair.png"));
        crosshairSprite = new Sprite(crosshair);
        crosshairSprite.setScale(3f);

        Texture skyboxTexture = new Texture(Gdx.files.internal("skybox.png"), true);
        skyboxTexture.setFilter(Texture.TextureFilter.MipMapNearestNearest, Texture.TextureFilter.Nearest);
        Material skybox1 = new Material("skybox", new TextureAttribute(TextureAttribute.Diffuse, skyboxTexture), new BlendingAttribute(0.5f), new IntAttribute(IntAttribute.CullFace, GL20.GL_NONE));
        Material sunBox1 = new Material(ColorAttribute.createDiffuse(Color.GREEN));
        ModelBuilder modelBuilder = new ModelBuilder();
        Model box = modelBuilder.createBox(10, 10, 10, skybox1, VertexAttributes.Usage.Position | VertexAttributes.Usage.TextureCoordinates | VertexAttributes.Usage.ColorUnpacked);

        skybox = new ModelInstance(box);
        skybox.materials.get(0).set(TextureAttribute.createDiffuse(skyboxTexture));
        skybox.materials.get(0).set(new DepthTestAttribute(0, false));

        enableAnisotropy();
    }

    private ShaderProgram setupShaders() {
        ShaderProgram.pedantic = true;
        ShaderProgram shaderProgram = new ShaderProgram(Gdx.files.internal("shaders/shader.vs"), Gdx.files.internal("shaders/shader.fs"));
        System.out.println(shaderProgram.isCompiled() ? "Shaders compiled ok" : "Shaders didn't compile ok: " + shaderProgram.getLog());

        return shaderProgram;
    }

    private void setupRendering(Material material) {
        Gdx.gl.glEnable(GL20.GL_CULL_FACE);
        Gdx.gl.glCullFace(GL20.GL_BACK);
        shaderProgram = setupShaders();

        Material alphamat = new Material("AlphaMaterial1", new TextureAttribute(TextureAttribute.Diffuse, textureAtlas.getTextures().first()), new BlendingAttribute(1.0f), new FloatAttribute(FloatAttribute.AlphaTest, 0.0f));

        voxelBatch = new ModelBatch(new DefaultShaderProvider() {
            @Override
            protected Shader createShader(Renderable renderable) {
                Gdx.app.log("DefaultShaderProvider", "Creating new shader");
                return new DefaultShader(renderable, new DefaultShader.Config(), shaderProgram);
            }
        });

        voxelRender = new VoxelRender(material, world, camera, false);
        alphaVoxelRender = new VoxelRender(alphamat, world, camera, true);

        skyboxRender = new ModelBatch();

        spriteBatch = new SpriteBatch();
    }

    public TextureAtlas getTextureAtlas() {
        return textureAtlas;
    }

    public void setSkyColor(float r, float g, float b, float a) {
        skyFog.set(r, g, b, a);
        skyFogColorAttribute.color.set(skyFog);
    }

    private Material setupMaterialAndEnvironment() {
        environment = new Environment();
        //environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.8f, 0.8f, 0.8f, 1f));
        //environment.set(new ColorAttribute(ColorAttribute.Fog, 13 / 255f, 41 / 255f, 121 / 255f, 0));
        return new Material("Material1", new TextureAttribute(TextureAttribute.Diffuse, textureAtlas.getTextures().first()));
    }


    private void setupCameraController() {
        cameraController = new CameraController(camera);
        //cameraController = new FlyingCameraController(camera);
        cameraController.setVelocity(7.5f);
        Gdx.input.setInputProcessor(cameraController);
        Gdx.input.setCursorCatched(true);
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
