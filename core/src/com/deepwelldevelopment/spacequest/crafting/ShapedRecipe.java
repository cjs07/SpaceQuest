package com.deepwelldevelopment.spacequest.crafting;

import com.deepwelldevelopment.spacequest.item.ItemStack;

public class ShapedRecipe implements IRecipe {

    private int size;
    private int width;
    private int height;
    private ItemStack[] ingredients;
    private ItemStack result;

    public ShapedRecipe(int width, int height, ItemStack[] ingredients, ItemStack result) {
        this.width = width;
        this.height = height;
        this.ingredients = ingredients;
        this.result = result;
        this.size = ingredients.length;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public int width() {
        return width;
    }

    @Override
    public int height() {
        return height;
    }

    @Override
    public ItemStack[] getIngredients() {
        return ingredients;
    }

    @Override
    public ItemStack getResult() {
        return result;
    }

    @Override
    public boolean matches(CraftingMatrix matrix) {
        if (matrix.getWidth() < width || matrix.getHeight() < height) return false; //matrix is too small
        if (matrix.getRecipeWidth() != width || matrix.getRecipeHeight() != height) return false; //recipe size mismatch
        for (int i = 0; i < ingredients.length; i++) {
            if (ingredients[i].getItem() != matrix.getIngredient(i).getItem()) return false; //ingredient mismatch
        }
        return true; //found no reason for recipes to mismatch
    }
}
