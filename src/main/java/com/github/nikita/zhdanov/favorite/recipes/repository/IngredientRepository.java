package com.github.nikita.zhdanov.favorite.recipes.repository;

import com.github.nikita.zhdanov.favorite.recipes.model.Ingredient;
import org.springframework.data.repository.CrudRepository;

public interface IngredientRepository extends CrudRepository<Ingredient, String> {}
