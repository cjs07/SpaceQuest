package com.deepwelldevelopment.spacequest.block;

public class BlockProvider implements IBlockProvider {

    public static final Block air = new Block((byte) 0, "");
    public static final Block limeStone = new Block((byte) 1, "limestone");
    public static final Block grass = new Block((byte) 2, "grass_top", "dirt", "grass_side");
    public static final Block light = new Block((byte) 3, "glass").setOpacity(0).setLightSource(true);
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
//        addBlock(flower);
//        addBlock(water);
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
