package com.github.nikita.zhdanov.favorite.recipes.service;

import com.github.nikita.zhdanov.favorite.recipes.error.RecipeNotExists;
import com.github.nikita.zhdanov.favorite.recipes.model.Recipe;
import com.github.nikita.zhdanov.favorite.recipes.repository.RecipeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

@Service
public class RecipeService {
    @Autowired
    private RecipeRepository recipeRepository;

    public void save(Recipe recipe) {
        recipeRepository.save(recipe);
    }

    public void delete(String id) {
        try {
            recipeRepository.deleteById(id);
        } catch (EmptyResultDataAccessException e) {
            throw new RecipeNotExists(id);
        }
    }
}
