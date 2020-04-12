package com.deepwelldevelopment.spacequest;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.*;
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
import com.deepwelldevelopment.spacequest.client.gui.Gui;
import com.deepwelldevelopment.spacequest.client.render.VoxelRender;
import com.deepwelldevelopment.spacequest.inventory.InventoryPlayer;
import com.deepwelldevelopment.spacequest.item.Item;
import com.deepwelldevelopment.spacequest.item.ItemBlock;
import com.deepwelldevelopment.spacequest.item.ItemStack;
import com.deepwelldevelopment.spacequest.physics.PhysicsController;
import com.deepwelldevelopment.spacequest.world.World;
import com.deepwelldevelopment.spacequest.world.biome.IBiomeProvider;
import com.deepwelldevelopment.spacequest.world.biome.OverworldBiomeProvider;
import com.deepwelldevelopment.spacequest.world.chunk.IChunkProvider;

import java.nio.FloatBuffer;

/**
 * Main app class and gdx entry point.
 */
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
    private Item item;

    private Gui openGui;

    private InventoryPlayer playerInventory;
    private Sprite hotbarUnselectedSprite;
    private Sprite hotbarSelectedSprite;
    private Sprite[] hotbarSprites;

    public static SpaceQuest getSpaceQuest() {
        return spaceQuest;
    }

    public void openGui(Gui gui) {
        this.openGui = gui;
        cameraController.releaseCursor();
    }

    public void closeGui() {
        this.openGui = null;
    }

    public Gui getOpenGui() {
        return openGui;
    }

    public boolean isGuiOen() {
        return openGui != null;
    }

    /**
     * Clears the OpenGL rendering environment
     */
    private void clearOpenGL() {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        Vector3 direction = camera.direction;
        float v = Math.abs(MathUtils.atan2(direction.x, direction.z) * MathUtils.radiansToDegrees);
        v = Math.min(90, v / 2);

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        Gdx.gl.glClearColor((v / 2) / 255f, (v / 1) / 255f, (255 - v) - 50 / 255f, 1);
        setSkyColor((v / 2) / 255f, (v / 1) / 255f, (255 - v) - 50 / 255f, 1);
    }

    /**
     * Called when the {@link Application} is first created.
     */
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
        this.playerInventory = new InventoryPlayer();

        item = new Item((byte) 1, "diamond");
    }

    /**
     * Called when the {@link Application} is resized. This can happen at any point during a
     * non-paused state but will never happen before a call to {@link #create()}.
     *
     * @param width  the new width in pixels
     * @param height the new height in pixels
     */
    @Override
    public void resize(int width, int height) {
        createCamera(width, height);
        setup();
    }

    /**
     * Called when the {@link Application} should render itself.
     */
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

    /**
     * Called when the {@link Application} is paused, usually when it's not active or visible
     * on-screen. An Application is also paused before it is destroyed.
     */
    @Override
    public void pause() {
    }

    /**
     * Called when the {@link Application} is resumed from a paused state, usually when it
     * regains focus.
     */
    @Override
    public void resume() {
    }

    /**
     * Called when the {@link Application} is destroyed. Preceded by a call to {@link #pause()}.
     */
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

    /**
     * Renders the models. Handles
     */
    private void renderModelBatches() {
        if (!world.isPlayerInWater(camera)) {
            skyboxRender.begin(camera);
            skyboxRender.render(skybox);
            skyboxRender.end();
        }
        renderVoxelBatch();
    }

    /**
     * Sets up the environment and renders the world
     */
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

    public PhysicsController getPhysicsController() {
        return physicsController;
    }

    /**
     * Renders the 2D screen on top of the world. This includes the crosshair and on screen GUIs
     */
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
        renderHotbar(spriteBatch);
        if (openGui != null) {
            openGui.render(spriteBatch, Gdx.input.getX(), Gdx.input.getY());
        }
        spriteBatch.end();
    }

    public InventoryPlayer getPlayerInventory() {
        return playerInventory;
    }

    private void renderHotbar(Batch batch) {
        int x = (int) ((Gdx.graphics.getWidth() / 2) -
                ((hotbarUnselectedSprite.getWidth() * hotbarUnselectedSprite.getScaleX() *
                        InventoryPlayer.HOTBAR_SIZE) / 2));
        hotbarSelectedSprite.setPosition(x +
                (hotbarSelectedSprite.getWidth() * hotbarSelectedSprite.getScaleX() *
                        playerInventory.getSelectedSlot()), 50);
        for (int i = 0; i < playerInventory.getHotbar().size(); i++) {
            ItemStack stack = playerInventory.getHotbar().get(i);
            Sprite sprite = hotbarSprites[i];
            sprite.draw(batch);
            if (stack == ItemStack.EMPTY) continue;
            float itemstackX =
                    sprite.getX() + (sprite.getWidth() / 2) - (stack.getSprite().getWidth() / 2);
            float itemstackY =
                    sprite.getY() + (sprite.getHeight() / 2) - (stack.getSprite().getHeight() / 2);
            stack.render(batch, itemstackX, itemstackY, sprite.getX(), sprite.getY(),
                    sprite.getWidth()
            );
        }
        hotbarSelectedSprite.draw(batch);
    }

    private void tickPhysics(float delta) {
        physicsController.update(delta);
        camera.update(true);
    }

    public BitmapFont getFont() {
        return font;
    }

    /**
     * Sets up all the necessary pieces for rendering. This method does no instance-based setup
     * (world, etc.)
     */
    private void setup() {
        if (assetManager.isLoaded("blocks.atlas")) {
            assetManager.unload("blocks.atlas");
        }
        assetManager.load("blocks.atlas", TextureAtlas.class);
        textureAtlas = new TextureAtlas(Gdx.files.internal("blocks.atlas"));

        font = new BitmapFont();
        Material material = setupMaterialAndEnvironment();
        setupRendering(material);

        crosshair = new Texture(Gdx.files.internal("crosshair.png"));
        crosshairSprite = new Sprite(crosshair);
        item.setSprite(textureAtlas.createSprite("diamond"));

        hotbarSprites = new Sprite[InventoryPlayer.HOTBAR_SIZE];
        hotbarUnselectedSprite = textureAtlas.createSprite("hotbar_unselected");
        hotbarUnselectedSprite.setScale(2, 2);
        hotbarSelectedSprite = textureAtlas.createSprite("hotbar_selected");
        hotbarSelectedSprite.setScale(2, 2);
        int startX = (int) ((Gdx.graphics.getWidth() / 2) -
                ((hotbarUnselectedSprite.getWidth() * hotbarUnselectedSprite.getScaleX() *
                        InventoryPlayer.HOTBAR_SIZE) / 2));
        System.out.println(startX);
        Sprite sprite = hotbarSelectedSprite;
        sprite.setPosition(startX, 50);
        hotbarSprites[0] = sprite;
        for (int i = 0; i < hotbarSprites.length; i++) {
            sprite = new Sprite(hotbarUnselectedSprite);
            sprite.setPosition(startX + (sprite.getWidth() * sprite.getScaleX() * i), 50);
            hotbarSprites[i] = sprite;
        }

        playerInventory.setStackInSlot(new ItemStack(new ItemBlock(BlockProvider.grass)), 0);
        playerInventory.setStackInSlot(new ItemStack(new ItemBlock(BlockProvider.light)), 1);
        playerInventory.setStackInSlot(new ItemStack(new ItemBlock(BlockProvider.dirt)), 2);
        playerInventory.setStackInSlot(new ItemStack(new ItemBlock(BlockProvider.glass)), 3);
        playerInventory.setStackInSlot(new ItemStack(new ItemBlock(BlockProvider.stone)), 4);
        playerInventory.setStackInSlot(new ItemStack(new ItemBlock(BlockProvider.wall)), 5);
        playerInventory.setStackInSlot(new ItemStack(new ItemBlock(BlockProvider.treeTrunk)), 6);
        playerInventory.setStackInSlot(new ItemStack(new ItemBlock(BlockProvider.sand)), 7);
        playerInventory.setStackInSlot(new ItemStack(new ItemBlock(BlockProvider.sandStone)), 8);

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

    /**
     * Loads and compiles the shaders
     *
     * @return The compiled shader program
     */
    private ShaderProgram setupShaders() {
        ShaderProgram.pedantic = true;
        ShaderProgram shaderProgram = new ShaderProgram(Gdx.files.internal("shaders/shader.vert"),
                Gdx.files.internal("shaders/shader.frag")
        );
        System.out.println(shaderProgram.isCompiled() ? "Shaders compiled ok" : "Sahders failed " +
                "to compile: " + shaderProgram.getLog());
        return shaderProgram;
    }

    /**
     * Initializes and configures rendering
     *
     * @param material The primary rendering material
     */
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

    /**
     * Sets the color of the sky
     *
     * @param r Red
     * @param g Green
     * @param b Blue
     * @param a Alpha
     */
    private void setSkyColor(float r, float g, float b, float a) {
        skyFog.set(r, g, b, a);
        skyFogColorAttribute.color.set(skyFog);
    }

    /**
     * Sets up the material and environment
     *
     * @return The material
     */
    private Material setupMaterialAndEnvironment() {
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.8f, 0.8f, 0.8f, 1f));
        environment.set(new ColorAttribute(ColorAttribute.Fog, 13 / 255f, 41 / 255f, 121 /
                255f, 0));
        return new Material("Material1",
                new TextureAttribute(TextureAttribute.Diffuse, textureAtlas.getTextures().first())
        );
    }

    /**
     * Sets up the custom camera controller
     */
    private void setupCameraController() {
        cameraController = new CameraController(camera, physicsController, playerInventory);
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

    //TODO: is this method needed?
    private void createCamera(int width, int height) {
        camera.viewportWidth = width;
        camera.viewportHeight = height;
    }
}
