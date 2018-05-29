package com.deepwelldevelopment.spacequest.block;

public class BlockProvider implements IBlockProvider {
//    public final static Block air = new Air((byte) 0);
//    public final static Block limeStone = new StoneBlock((byte) 1, "textures/limestone");
//    public final static Block grass = new GrassBlock((byte) 2, "textures/grass_top", "textures/dirt", "textures/grass_sides");
//    public final static Block light = new LightBlock((byte) 3, "textures/lightbox");
//    public final static Block glass = new GlassBlock((byte) 4, "textures/lightbox");
//    public final static Block wall = new WallBlock((byte) 5, "textures/wall");
//    public final static Block treeTrunk = new TreeTrunkBlock((byte) 6, "textures/trunk_top", "textures/trunk_top", "textures/trunk");
//    public final static Block leaves = new LeavesBlock((byte) 7, "textures/uncolored_leaves");
//    public final static Block straws = new Straw((byte) 8, "textures/straw");
//    public final static Block flower = new FlowerBlock((byte) 9, "textures/flower");
//    public final static Block water = new WaterBlock((byte) 10, "textures/water");
//    public final static Block shale = new StoneBlock((byte) 11, "textures/shale");
//    public final static Block sandStone = new StoneBlock((byte) 12, "textures/sandstone");

    public static final Block air = new Block((byte) 0, "", "", "");
    public static final Block limeStone = new Block((byte) 1, "limestone");
    public static final Block grass = new Block((byte) 2, "grass_top", "dirt", "grass_side");
    public static final Block light = new Block((byte) 3, "glass").setOpacity(0).setIsLightSource(true);
    public static final Block glass = new Block((byte) 4, "glass").setOpacity(8);
    public static final Block wall = new Block((byte) 5, "wall");
    public static final Block treeTrunk = new Block((byte) 6, "trunk_top", "trunk_top", "trunk");
    public static final Block leaves = new Block((byte) 7, "leaves");
    //    public static final Block straws = new Block((byte) 8, "straw");
    public static final Block straws = new Block((byte) 8, "leaves").setOpacity(0).setCollidable(false);
    public static final Block flower = new BlockFlower((byte) 9, "flower");
    public static final Block water = new BlockWater((byte) 10, "water").setOpacity(24).setCollidable(false);
    //    public static final Block shale = new Block((byte) 11, "shale");
    public static final Block shale = new Block((byte) 11, "limestone");
    //    public static final Block sandStone = new Block((byte) 12, "sandstone");
    public static final Block sandStone = new Block((byte) 12, "limestone");
    public static final Block stone = new Block((byte) 13, "stone");
    public static final Block sand = new Block((byte) 14, "sand");
    public static final Block dirt = new Block((byte) 15, "dirt");

    private final Block[] blocks = new Block[128];

    public BlockProvider() {
        addBlock(air);
        addBlock(limeStone);
        addBlock(shale);
        addBlock(sandStone);
        addBlock(grass);
        addBlock(light);
        addBlock(glass);
        addBlock(wall);
        addBlock(treeTrunk);
        addBlock(leaves);
        addBlock(straws);
        addBlock(flower);
        addBlock(water);
        addBlock(stone);
        addBlock(sand);
        addBlock(dirt);
    }

    private void addBlock(Block block) {
        blocks[block.getId()] = block;
    }

    @Override
    public Block getBlockById(byte blockId) {
        return blocks[blockId];
    }
}
