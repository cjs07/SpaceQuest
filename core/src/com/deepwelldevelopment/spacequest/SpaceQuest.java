package com.deepwelldevelopment.spacequest;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.*;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader.Config;
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
import com.deepwelldevelopment.spacequest.physics.PhysicsController;
import com.deepwelldevelopment.spacequest.world.World;
import com.deepwelldevelopment.spacequest.world.biome.IBiomeProvider;
import com.deepwelldevelopment.spacequest.world.biome.OverworldBiomeProvider;
import com.deepwelldevelopment.spacequest.world.chunk.IChunkProvider;

import java.nio.FloatBuffer;

public class SpaceQuest implements ApplicationListener {

    public static final int MAX_UPDATE_ITERATIONS = 10;
    public static final float fixedTimeStep = 1 / 60f;
    private static SpaceQuest spaceQuest;

    private AssetManager assetManager;
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
    private PhysicsController physicsController;
    //TODO: item?

    //TODO: openGui

    //TODO: inventory and hotbar

    public static SpaceQuest getSpaceQuest() {
        return spaceQuest;
    }

    //TODO: openGui

    //TODO: closeGui

    //TODO: getOpenGui

    //TODO: isGuiOpen

    private void clearOpenGL() {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        Vector3 direction = camera.direction;
        float v = Math.abs(MathUtils.atan2(direction.x, direction.z) * MathUtils.radiansToDegrees);
        v = Math.min(90, v / 2);

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        Gdx.gl.glClearColor((v / 2) / 255f, (v / 1) / 255f, (255 - v) - 50 / 255f, 1);
        setSkyColor((v / 2) / 255f, (v / 1) / 255f, (255 - v) - 50 / 255f, 1);
    }

    @Override
    public void create() {
        spaceQuest = this;
        assetManager = new AssetManager();
        camera = new PerspectiveCamera(70f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.near = 0.1f;
        camera.far = 200;
        camera.position.set(new Vector3(0, 100, 0));
        camera.lookAt(0, 100, 0);
        camera.rotate(camera.up, 182);
        camera.update();

        this.blockProvider = new BlockProvider();
        this.biomeProvider = new OverworldBiomeProvider();
        this.world = new World(blockProvider, biomeProvider);
        this.physicsController = new PhysicsController(world, camera);
        this.chunkProvider = world.getChunkProvider();
        //TODO: player inventory and item?
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
        assetManager.dispose();
        voxelBatch.dispose();
        spriteBatch.dispose();
//        crosshair.dispose();
        shaderProgram.dispose();
        font.dispose();
    }

    public World getWorld() {
        return world;
    }

    private void renderModelBatches() {
        //TODO: special behavior if palyer is in water?
        renderVoxelBatch();
    }

    private void renderVoxelBatch() {
        //TODO: special behavior if player is in water
        shaderProgram.begin();
        shaderProgram.setUniformf("u_fogstr", 0.04f);
        environment.set(skyFogColorAttribute);

        voxelBatch.begin(camera);
        voxelBatch.render(voxelRender, environment);
        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
        voxelBatch.render(alphaVoxelRender, environment);
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        voxelBatch.end();
    }

    public PhysicsController getPhysicsController() {
        return physicsController;
    }

    private void renderSpriteBatches() {
        spriteBatch.begin();
        font.draw(spriteBatch, "fps: " + Gdx.graphics.getFramesPerSecond() + " - visible/total " +
                "chunks: " + VoxelRender.getNumberOfVisibleChunks() + "/" +
                VoxelRender.getNumberOfChunks() + " - visible/total blocks: " +
                VoxelRender.getNumberOfVisibleBlocks() + "/" + VoxelRender.getBlockCounter() +
                " -" +
                " visible vertices: " + VoxelRender.getNumberOfVertices() + " - visibile indices:" +
                " " + VoxelRender.getNumberOfIndices() + " x: " + camera.position.x + " y: " +
                camera.position.y + " z: " + camera.position.z, 0, 20);
        crosshairSprite.setPosition(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);
        crosshairSprite.draw(spriteBatch);
        //TODO: render hotbar
        //TODO: GUI render
        spriteBatch.end();
    }

    //TODO: getPlayerInventory

    //TODO: render hotbar

    private void tickPhysics(float delta) {
        physicsController.update(delta);
        camera.update(true);
    }

    public BitmapFont getFont() {
        return font;
    }

    private void setup() {
        if (assetManager.isLoaded("blocks.atlas")) {
            assetManager.unload("blocks.atlas");
        }
        assetManager.load("blocks.atlas", TextureAtlas.class);
        textureAtlas = new TextureAtlas(Gdx.files.internal("blocks.atlas"));

        font = new BitmapFont();
        Material material = setupMaterialAndEnvironemnt();
        setupRendering(material);

        crosshair = new Texture(Gdx.files.internal("crosshair.png"));
        crosshairSprite = new Sprite(crosshair);
        //TODO: sprites

        setupCameraController();

        Texture skyboxTexture = new Texture(Gdx.files.internal("skybox.png"));
        skyboxTexture.setFilter(TextureFilter.MipMapLinearNearest, TextureFilter.Nearest);
        Material skybox1 = new Material("skybox", new TextureAttribute(TextureAttribute.Diffuse,
                skyboxTexture
        ), new BlendingAttribute(0.5f),
                new IntAttribute(IntAttribute.CullFace, GL20.GL_NONE)
        );
        Material sunbox1 = new Material(ColorAttribute.createDiffuse(Color.GREEN));
        ModelBuilder modelBuilder = new ModelBuilder();
        Model box = modelBuilder.createBox(10, 10, 10, skybox1,
                Usage.Position | Usage.TextureCoordinates | Usage.ColorUnpacked
        );

        skybox = new ModelInstance(box);
        skybox.materials.get(0).set(TextureAttribute.createDiffuse(skyboxTexture));
        skybox.materials.get(0).set(new DepthTestAttribute(0, false));

        enableAnisotropy();
    }

    private ShaderProgram setupShaders() {
        ShaderProgram.pedantic = true;
        ShaderProgram shaderProgram = new ShaderProgram(Gdx.files.internal("shaders/shader.vert"),
                Gdx.files.internal("shaders/shader.frag")
        );
        System.out.println(shaderProgram.isCompiled() ? "Shaders compiled ok" : "Sahders failed " +
                "to compile: " + shaderProgram.getLog());
        return shaderProgram;
    }

    private void setupRendering(Material material) {
        Gdx.gl.glEnable(GL20.GL_CULL_FACE);
        Gdx.gl.glCullFace(GL20.GL_BACK);
        shaderProgram = setupShaders();

        Material alphaMat = new Material("AlphaMaterial1",
                new TextureAttribute(TextureAttribute.Diffuse,
                        textureAtlas.getTextures().first()
                ), new BlendingAttribute(1.0f),
                new FloatAttribute(FloatAttribute.AlphaTest, 0.0f)
        );
        voxelBatch = new ModelBatch(new DefaultShaderProvider() {
            @Override
            protected Shader createShader(Renderable renderable) {
                Gdx.app.log("DefaultShaderProvider", "Creating new shader");
                return new DefaultShader(renderable, new Config(), shaderProgram);
            }
        });

        voxelRender = new VoxelRender(camera, material, false, world);
        alphaVoxelRender = new VoxelRender(camera, alphaMat, false, world);
        skyboxRender = new ModelBatch();
        spriteBatch = new SpriteBatch();
    }

    public TextureAtlas getTextureAtlas() {
        return textureAtlas;
    }

    private void setSkyColor(float r, float g, float b, float a) {
        skyFog.set(r, g, b, a);
        skyFogColorAttribute.color.set(skyFog);
    }

    private Material setupMaterialAndEnvironemnt() {
        environment = new Environment();
        //environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.8f, 0.8f, 0.8f, 1f));
        //environment.set(new ColorAttribute(ColorAttribute.Fog, 13 / 255f, 41 / 255f, 121 /
        // 255f, 0));
        return new Material("Material1",
                new TextureAttribute(TextureAttribute.Diffuse, textureAtlas.getTextures().first())
        );
    }

    private void setupCameraController() {
        cameraController = new CameraController(camera, physicsController);
        cameraController.setVelocity(120f);
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
            Gdx.gl.glTexParameteri(GL20.GL_TEXTURE_2D, GL30.GL_TEXTURE_MAX_LEVEL, 4);
        }
    }

    private void createCamera(int width, int height) {
        camera.viewportWidth = width;
        camera.viewportHeight = height;
    }
}
