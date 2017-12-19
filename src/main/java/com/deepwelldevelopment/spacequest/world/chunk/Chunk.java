package com.deepwelldevelopment.spacequest.world.chunk;

import com.deepwelldevelopment.spacequest.renderer.ChunkRenderer;
import com.deepwelldevelopment.spacequest.world.World;

public class Chunk {

    World world;

    int x;
    int z;

    private boolean initialized;

    int averageHeight = 16;

    int[][] heightmap;
    Layer[] layers;

    private ChunkRenderer renderer;

    public Chunk(World world, int x, int z) {
        this .world = world;
        this.x = x;
        this.z = z;

        heightmap = new int[16][16];

        layers = new Layer[32];

        renderer = new ChunkRenderer(this);
        initialized = false;
    }

    public void generateLayers() {
        for (int i = 0; i < layers.length; i++) {
            layers[i] = new Layer(this,16, i);
        }
    }

    /***
     * Adjusts the heightmap of the chunk at a given (x, z) column
     * @param heightPercent
     * @param x
     * @param z
     */
    public void adjustHeight(double heightPercent, int x, int z) {
        heightmap[x][z] = averageHeight + (int)Math.floor(averageHeight * heightPercent);
        System.out.print(heightmap[x][z] + " ");
    }

    public int getHeight(int x, int z) {
        return heightmap[x][z];
    }

    public void initBlocks() {
        if (!initialized) {
            for (Layer layer : layers) {
                layer.initBlocks();
            }
            renderer.init();
//            renderer.update();
            initialized = true;
        }
    }

    public void render() {
//        for (Layer layer : layers) {
//            layer.render();
//        }
        renderer.render();
    }

    public Layer getLayer(int i) {
        return layers[i];
    }

    public Layer[] getLayers() {
        return layers;
    }

    public int getMaxHeight() {
        return layers.length;
    }

    public void cleanup() {
        for (Layer layer : layers) {
            layer.cleanup();
        }
    }
}
