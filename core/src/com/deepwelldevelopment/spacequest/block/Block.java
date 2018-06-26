package com.deepwelldevelopment.spacequest.block;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.deepwelldevelopment.spacequest.client.render.IBlockRender;
import com.deepwelldevelopment.spacequest.client.render.block.BasicBlockRender;
import com.deepwelldevelopment.spacequest.item.Item;
import com.deepwelldevelopment.spacequest.item.ItemBlock;
import com.deepwelldevelopment.spacequest.item.ItemStack;
import com.deepwelldevelopment.spacequest.util.TextureUtils;
import com.deepwelldevelopment.spacequest.world.World;
import com.deepwelldevelopment.spacequest.world.chunk.Chunk;

public class Block {

    private final byte id;
    private final String topTextureRegion;
    private final String bottomTextureRegion;
    private final String sidesTextureRegion;
    protected IBlockRender blockRender;
    private Vector2[] topTextureUVs;
    private Vector2[] bottomTextureUVs;
    private Vector2[] sidesTextureUVs;

    private int opacity;
    private boolean isLightSource;
    private Color tileColor;
    private boolean collidable;
    private float hardness;

    public Block(byte id, String topTextureRegion, String bottomTextureRegion, String sidesTextureRegion) {
        this.id = id;
        this.topTextureRegion = topTextureRegion;
        this.bottomTextureRegion = bottomTextureRegion;
        this.sidesTextureRegion = sidesTextureRegion;
        this.blockRender = new BasicBlockRender();
        this.opacity = 32;
        this.isLightSource = false;
        this.tileColor = Color.WHITE;
        this.collidable = true;
        this.hardness = 1.0f;
        if (!topTextureRegion.equals("") && !bottomTextureRegion.equals("") && !sidesTextureRegion.equals("")) { //make sure air does not get an itemblock
            new ItemBlock(this);
        }
    }

    public Block(byte id, String textureRegion) {
        this(id, textureRegion, textureRegion, textureRegion);
    }

    public float getHardness() {
        return hardness;
    }

    public byte getId() {
        return id;
    }

    public String getTopTextureRegion() {
        return topTextureRegion;
    }

    public Vector2[] getTopTextureUVs() {
        if (topTextureUVs == null) {
            topTextureUVs = TextureUtils.calculateUVMapping(topTextureRegion);
        }
        return topTextureUVs;

    }

    public Vector2[] getBottomTextureUVs() {
        if (bottomTextureUVs == null) {
            bottomTextureUVs = TextureUtils.calculateUVMapping(bottomTextureRegion);
        }
        return bottomTextureUVs;
    }

    public Vector2[] getSidesTextureUVs() {
        if (sidesTextureUVs == null) {
            sidesTextureUVs = TextureUtils.calculateUVMapping(sidesTextureRegion);
        }
        return sidesTextureUVs;
    }

    public final boolean drawSide(IBlockProvider blockProvider, Chunk chunk, int x, int y, int z, Side side) {
        if (y == 0 && side == Side.BOTTOM) {
            return false;
        }
        return blockRenderSide(blockProvider, chunk, x, y, z, side);
    }

    /**
     * Determines if the block should render a given side
     *
     * @param blockProvider
     * @param chunk
     * @param x
     * @param y
     * @param z
     * @param side
     * @return
     */
    protected boolean blockRenderSide(IBlockProvider blockProvider, Chunk chunk, int x, int y, int z, Side side) {
        byte blockAtSide = side.getBlockAt(chunk, x, y, z);
        if (blockAtSide == 0) {
            return true;
        }
        return blockProvider.getBlockById(blockAtSide).getOpacity() < 32;
    }

    public void onNeighborBlockChange(World world, int x, int y, int z) {
    }

    public boolean isLightSource() {
        return isLightSource;
    }

    public int getOpacity() {
        return opacity;
    }

    public Block setOpacity(int opacity) {
        this.opacity = opacity;
        return this;
    }

    public Color getTileColor(int x, int y, int z) {
        return tileColor;
    }

    public IBlockRender getBlockRender() {
        return blockRender;
    }

    public boolean isCollidable() {
        return collidable;
    }

    public Block setCollidable(boolean collidable) {
        this.collidable = collidable;
        return this;
    }

    public boolean isLiquid() {
        return false;
    }

    public Block setIsLightSource(boolean lightSource) {
        this.isLightSource = lightSource;
        return this;
    }

    public Block setTileColor(Color tileColor) {
        this.tileColor = tileColor;
        return this;
    }

    public ItemStack getDrop(int x, int y, int z) {
        return new ItemStack(Item.getItemFromBlock(this));
    }

    public boolean onBlockActivated(int x, int y, int z) {
        return false;
    }

    public enum Side {
        FRONT(new Vector3(0, 0, 1)),
        BACK(new Vector3(0, 0, -1)),
        RIGHT(new Vector3(1, 0, 0)),
        LEFT(new Vector3(-1, 0, 0)),
        TOP(new Vector3(0, 1, 0)),
        BOTTOM(new Vector3(0, -1, 0));

        private Vector3 sideDirection;

        Side(Vector3 sideDirection) {
            this.sideDirection = sideDirection;
        }

        public byte getBlockAt(Chunk chunk, int x, int y, int z) {
            return chunk.getByte(x + (int) sideDirection.x, y + (int) sideDirection.y, z + (int) sideDirection.z);
        }
    }
}
