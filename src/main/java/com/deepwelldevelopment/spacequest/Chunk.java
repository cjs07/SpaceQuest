package com.deepwelldevelopment.spacequest;

public class Chunk {

    World world;

    int x;
    int z;

    int averageHeight = 16;

    int[][] heightmap;
    Layer[] layers;

    public Chunk(World world, int x, int z) {
        this .world = world;
        this.x = x;
        this.z = z;

        heightmap = new int[16][16];

        layers = new Layer[32];
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
        for (Layer layer : layers) {
            layer.initBlocks();
        }
    }

    public void render() {
        for (Layer layer : layers) {
            layer.render();
        }
    }

    public Layer getLayer(int i) {
        return layers[i];
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
