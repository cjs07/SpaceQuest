package com.deepwelldevelopment.spacequest.crafting;

import com.deepwelldevelopment.spacequest.item.ItemStack;

public interface IRecipe {

    /**
     * Returns the size of the recipe (the number of ItemStacks contained)
     *
     * @return
     */
    int size();

    /**
     * Returns the width of the recipe (index of the last column to contain one more non-empty ItemStacks + 1)
     */
    int width();

    /**
     * Returns the height of the recipe (index of the last row to contain one or more non-empty ItemStacks + 1)
     */
    int height();

    /**
     * Returns the ingredients of this recipe
     */
    ItemStack[] getIngredients();

    /**
     * Returns the result of the recipe
     */
    ItemStack getResult();

    /**
     * Determines if the provided crafting matrix matches this recipe.
     */
    boolean matches(CraftingMatrix matrix);
}
