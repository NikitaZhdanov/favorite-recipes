package com.github.nikita.zhdanov.favorite.recipes.error;

public class RecipeNotExists extends RuntimeException {
    public RecipeNotExists(String id) {
        super("Recipe with id " + id + " not found");
    }
}
