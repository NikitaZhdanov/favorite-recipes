package com.github.nikita.zhdanov.favorite.recipes.repository;

import com.github.nikita.zhdanov.favorite.recipes.model.Recipe;
import org.springframework.data.repository.CrudRepository;

public interface RecipeRepository extends CrudRepository<Recipe, String> {}
