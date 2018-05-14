package com.deepwelldevelopment.spacequest;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.deepwelldevelopment.spacequest.block.Block;
import com.deepwelldevelopment.spacequest.block.BlockInstance;
import com.deepwelldevelopment.spacequest.block.BlockModel;

import java.util.ArrayList;

public class SpaceQuest implements ApplicationListener {

    private Environment environment;
    private PerspectiveCamera cam;
    private ModelBatch modelBatch;
    private BlockModel grassModel;
    private Texture texture;
    private Texture texture2;
    private ArrayList<BlockInstance> blockInstances;
    private ArrayList<Block> world;
    private CameraController camController;

    private Stage stage;
    private Label label;
    private BitmapFont font;
    private Image crosshair;
    private Texture crosshairTexture;

    @Override
    public void create() {
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));
        modelBatch = new ModelBatch();
        cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        cam.position.set(10f, 10f, 10f);
        cam.lookAt(0, 0, 0);
        cam.near = 1f;
        cam.far = 300f;
        cam.update();

        texture = new Texture("texture.png");
        texture2 = new Texture("texture2.png");

        grassModel = new BlockModel("grass", texture, texture, texture2, texture, texture, texture);

        world = new ArrayList<>();
        for (int x = 0; x < 10; x++) {
            for (int y = 0; y < 10; y++) {
                for (int z = 0; z < 10; z++) {
                    Block block = new Block(x, y, z);
                    world.add(block);
                }
            }
        }

        camController = new CameraController(cam);
        cam.up.set(0, 1, 0);
        Gdx.input.setInputProcessor(camController);
        Gdx.input.setCursorCatched(true);

        stage = new Stage();
        font = new BitmapFont();
        label = new Label(" ", new Label.LabelStyle(font, Color.WHITE));
        crosshairTexture = new Texture("crosshair.png");
        crosshair = new Image(crosshairTexture);
        crosshair.setPosition(Gdx.graphics.getWidth() / 2,Gdx.graphics.getHeight() / 2);
        stage.addActor(label);
        stage.addActor(crosshair);

        blockInstances = new ArrayList<>();
        for (int i = 0; i < world.size(); i++) {
            Block b = world.get(i);
            boolean front = false;
            boolean back = false;
            boolean top = false;
            boolean bottom = false;
            boolean left = false;
            boolean right = false;

            if (b.getX() == 0) {
                left = true;
            }
            if (b.getX() == 9) {
                right = true;
            }
            if (b.getY() == 0) {
                bottom = true;
            }
            if (b.getY() == 9) {
                top = true;
            }
            if (b.getZ() == 0) {
                front = true;
            }
            if (b.getZ() == 9) {
                back = true;
            }

            blockInstances.add(new BlockInstance(b, grassModel, front, back, top, bottom, left, right));
        }
    }

    @Override
    public void render() {
        camController.update();

        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        modelBatch.begin(cam);
        for (ModelInstance instance : blockInstances) {
            modelBatch.render(instance, environment);
        }
        modelBatch.end();

        label.setText("FPS: " + Gdx.graphics.getFramesPerSecond());
        stage.draw();
    }

    @Override
    public void dispose() {
        texture.dispose();
        texture2.dispose();
        grassModel.dispose();
        for (BlockInstance bi : blockInstances) {
            bi.dispose();
        }
        blockInstances.clear();
        modelBatch.dispose();
        font.dispose();
        crosshairTexture.dispose();
        stage.dispose();
    }

    @Override
    public void resume() {
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        cam.viewportWidth = width;
        cam.viewportHeight = height;
    }

    @Override
    public void pause() {
    }
}
