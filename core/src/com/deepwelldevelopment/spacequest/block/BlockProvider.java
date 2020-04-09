package com.deepwelldevelopment.spacequest.block;

public class BlockProvider implements IBlockProvider {

    public static final Block air = new Block((byte) 0, "");
    public static final Block stone = new Block((byte) 1, "stone");

    private final Block[] blocks = new Block[128];

    public BlockProvider() {
        addBlock(air);
        addBlock(stone);
    }

    private void addBlock(Block block) {
        blocks[block.getId()] = block;
    }

    @Override
    public Block getBlockById(byte blockId) {
        return blocks[blockId];
    }
}
