package com.deepwelldevelopment.spacequest.crafting;

import com.deepwelldevelopment.spacequest.block.Block;
import com.deepwelldevelopment.spacequest.item.Item;
import com.deepwelldevelopment.spacequest.item.ItemStack;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

public class CraftingManager {

    public static CraftingManager INSTANCE = new CraftingManager();

    private List<IRecipe> recipes = Lists.newArrayList();

    public void initRecipes() {

    }

    /**
     * Calculates and adds a new recipe to the game. A shaped recipe is represented by a series of strings followed by
     * pairs of characters and ItemStacks showing what the strings represent
     *
     * @param result           The ItemStack that this recipe will craft into
     * @param recipeComponents The recipe parameters
     * @return The created recipe
     */
    public IRecipe addRecipe(ItemStack result, Object... recipeComponents) {
        String recipe;
        StringBuilder builder = new StringBuilder();
        int width = 0;
        int height = 0;
        while (recipeComponents[height] instanceof String) {
            String s = (String) recipeComponents[height];
            width = s.length() > width ? s.length() : width;
            height++;
            builder.append(s);
        }
        recipe = builder.toString();
        Map<Character, ItemStack> definitions = Maps.newHashMap();
        for (int i = height; i < recipeComponents.length; i += 2) {
            char c = (char) recipeComponents[i];
            ItemStack stack = ItemStack.EMPTY;
            Object mappedTo = recipeComponents[i + 1];
            if (mappedTo instanceof Item) {
                stack = new ItemStack((Item) mappedTo, 1);
            } else if (mappedTo instanceof Block) {
                stack = new ItemStack((Block) mappedTo, 1);
            } else if (mappedTo instanceof ItemStack) {
                stack = (ItemStack) mappedTo;
            }
            definitions.put(c, stack);
        }
        ItemStack[] recipeStacks = new ItemStack[width * height];
        for (int i = 0; i < recipeStacks.length; i++) {
            char c = recipe.charAt(i);
            if (definitions.containsKey(c)) {
                recipeStacks[i] = definitions.get(c).copy();
            } else {
                recipeStacks[i] = ItemStack.EMPTY;
            }
        }
        ShapedRecipe shapedRecipe = new ShapedRecipe(width, height, recipeStacks, result);
        this.recipes.add(shapedRecipe);
        return shapedRecipe;
    }
}
