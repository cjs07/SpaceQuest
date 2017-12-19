package com.deepwelldevelopment.spacequest.renderer;

import com.deepwelldevelopment.spacequest.util.ShaderUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import static org.lwjgl.opengl.GL11.glDeleteTextures;

public class ResourceManager {

    public static final int TEXTURE_SIZE = 16;

    public static final int FILE_WIDTH = 2;
    public static final int FILE_HEIGHT = 2;

    public static final float TEXTURE_WIDTH_FLOAT = 1.0f / FILE_WIDTH;
    public static final float TEXTURE_HEIGHT_FLOAT = 1.0f / FILE_HEIGHT;

    public static ResourceManager INSTANCE;

    public Map<String, Texture> textures;
    public int textureSheet;

    public ResourceManager() {
        textures = new HashMap<>();

        INSTANCE = this;
    }

    public void loadTextures() throws IOException {
        textureSheet = ShaderUtil.loadTexture("textures.png");

        String[][] names = new String[FILE_WIDTH][FILE_HEIGHT];
        try {
            File nameFile = new File(Thread.currentThread().getContextClassLoader().getResource("textureNames.txn").toURI());
            Scanner scanner = new Scanner(nameFile);
            int i =0;
            int j = 0;
            while (scanner.hasNext()) {
                String s = scanner.nextLine();
                names[i][j] = s;
                j++;
                if (j >= FILE_HEIGHT) {
                    i++;
                    j = 0;
                }
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        BufferedImage img = ImageIO.read(Thread.currentThread().getContextClassLoader().getResource("textures.png"));
        int w = img.getWidth();
        int h = img.getHeight();

        for (int i = 0; i < FILE_WIDTH; i++) {
            for (int j = 0; j < FILE_HEIGHT; j++) {
                float u = j * ((float)TEXTURE_SIZE/w);
                float v = i * ((float)TEXTURE_SIZE/h);
                textures.put(names[i][j], new Texture(names[i][j], u, v));
            }
        }
    }

    public void cleanup() {
        glDeleteTextures(textureSheet);
    }
}
