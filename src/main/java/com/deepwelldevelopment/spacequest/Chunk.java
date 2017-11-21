package com.deepwelldevelopment.spacequest;

public class Chunk {

    int x;
    int z;

    Layer[] layers;

    public Chunk(int x, int z) {
        this.x = x;
        this.z = z;

        layers = new Layer[16];
        for (int i = 0; i < layers.length; i++) {
            layers[i] = new Layer(this,16, i);
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
