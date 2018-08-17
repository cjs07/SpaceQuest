package com.deepwelldevelopment.spacequest.crafting;

import com.deepwelldevelopment.spacequest.item.ItemStack;

import java.util.Arrays;

/**
 * Represents an ingame crafting grid of a predetermined size. A crafting matrix can always be used to craft any recipe
 * whose width or height is less than or equal to the width or height of the matrix. The crafting matrix does not
 * include an output slot.
 */
public class CraftingMatrix {

    private ItemStack[] ingredients;
    private int width;
    private int height;

    public CraftingMatrix(int width, int height) {
        this.width = width;
        this.height = height;
        ingredients = new ItemStack[width * height];
        Arrays.fill(ingredients, ItemStack.EMPTY);
    }

    /**
     * Gets the itemstack in the matrix at the specified row and column (zero-indexed)
     *
     * @param row the row to find
     * @param col The column to find
     * @return The item stack in the specified matrix slot
     */
    public ItemStack getIngredient(int row, int col) {
        return ingredients[col * width + row];
    }

    public ItemStack getIngredient(int index) {
        return ingredients[index];
    }

    public void setIngredient(int row, int col, ItemStack stack) {
        ingredients[col * width + row] = stack;
    }

    public void setIngredient(int index, ItemStack stack) {
        ingredients[index] = stack;
    }

    public ItemStack[] getIngredients() {
        return ingredients;
    }

    /**
     * Returns the actual width of the matrix (number of columns)
     *
     * @return The real width of the matrix
     */
    public int getWidth() {
        return width;
    }

    /**
     * Returns the actual height of the matrix (number of rows)
     *
     * @return The real height of the matrix
     */
    public int getHeight() {
        return height;
    }

    /**
     * Determines the recipe width for the matrix (the actual width of the recipe, as determined by the contents of the
     * matrix). Will never be greater than the width of the matrix, but can be 0
     *
     * @return The recipe width of the matrix
     */
    public int getRecipeWidth() {
        for (int i = width; i >= 0; i--) {
            for (int j = 0; j < height; j++) {
                if (getIngredient(i, j) != ItemStack.EMPTY) {
                    return i;
                }
            }
        }
        return 0;
    }

    /**
     * Determines the recipe height for the matrix (the actual height of the recipe, as determined by the contents of the
     * matrix). Will never be greater than the height of the matrix, but can be 0
     *
     * @return The recipe height of the matrix
     */
    public int getRecipeHeight() {
        for (int i = height; i >= 0; i--) {
            for (int j = 0; j < width; j++) {
                if (getIngredient(i, j) != ItemStack.EMPTY) {
                    return i;
                }
            }
        }
        return 0;
    }
}
