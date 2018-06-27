package com.deepwelldevelopment.spacequest.crafting;

import com.deepwelldevelopment.spacequest.SpaceQuestTest;
import com.deepwelldevelopment.spacequest.block.BlockProvider;
import com.deepwelldevelopment.spacequest.item.Item;
import com.deepwelldevelopment.spacequest.item.ItemStack;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CraftingManagerTest extends SpaceQuestTest {

    @Test
    public void addRecipe() {
        IRecipe recipe = CraftingManager.INSTANCE.addRecipe(new ItemStack(BlockProvider.dirt), "F", 'F', BlockProvider.grass);
        assertNotNull(recipe);
        assertEquals(1, recipe.width());
        assertEquals(1, recipe.height());
        assertEquals(1, recipe.size());
        assertEquals(recipe.getIngredients()[0].getItem(), Item.getItemFromBlock(BlockProvider.grass));
        assertEquals(recipe.getResult().getItem(), Item.getItemFromBlock(BlockProvider.dirt));

        IRecipe recipe1 = CraftingManager.INSTANCE.addRecipe(new ItemStack(BlockProvider.glass, 8), " s ", "s s", " s ", 's', BlockProvider.sand);
        assertNotNull(recipe1);
        assertEquals(3, recipe1.width());
        assertEquals(3, recipe1.height());
        assertEquals(9, recipe1.size());
        assertEquals(recipe1.getIngredients()[0], ItemStack.EMPTY);
        assertEquals(recipe1.getIngredients()[1].getItem(), Item.getItemFromBlock(BlockProvider.sand));
        assertEquals(recipe1.getIngredients()[2], ItemStack.EMPTY);
        assertEquals(recipe1.getIngredients()[3].getItem(), Item.getItemFromBlock(BlockProvider.sand));
        assertEquals(recipe1.getIngredients()[4], ItemStack.EMPTY);
        assertEquals(recipe1.getIngredients()[5].getItem(), Item.getItemFromBlock(BlockProvider.sand));
        assertEquals(recipe1.getIngredients()[6], ItemStack.EMPTY);
        assertEquals(recipe1.getIngredients()[7].getItem(), Item.getItemFromBlock(BlockProvider.sand));
        assertEquals(recipe1.getIngredients()[8], ItemStack.EMPTY);
        assertEquals(Item.getItemFromBlock(BlockProvider.glass), recipe1.getResult().getItem());
        assertEquals(8, recipe1.getResult().getStackSize());

        IRecipe recipe2 = CraftingManager.INSTANCE.addRecipe(new ItemStack(BlockProvider.light, 2), "ggg", "gcg", 'g', BlockProvider.glass, 'c', BlockProvider.treeTrunk);
        assertNotNull(recipe1);
        assertEquals(3, recipe2.width());
        assertEquals(2, recipe2.height());
        assertEquals(6, recipe2.size());
        assertEquals(Item.getItemFromBlock(BlockProvider.glass), recipe2.getIngredients()[0].getItem());
        assertEquals(Item.getItemFromBlock(BlockProvider.glass), recipe2.getIngredients()[1].getItem());
        assertEquals(Item.getItemFromBlock(BlockProvider.glass), recipe2.getIngredients()[2].getItem());
        assertEquals(Item.getItemFromBlock(BlockProvider.glass), recipe2.getIngredients()[3].getItem());
        assertEquals(Item.getItemFromBlock(BlockProvider.treeTrunk), recipe2.getIngredients()[4].getItem());
        assertEquals(Item.getItemFromBlock(BlockProvider.glass), recipe2.getIngredients()[5].getItem());
        assertEquals(Item.getItemFromBlock(BlockProvider.light), recipe2.getResult().getItem());
        assertEquals(2, recipe2.getResult().getStackSize());
    }
}