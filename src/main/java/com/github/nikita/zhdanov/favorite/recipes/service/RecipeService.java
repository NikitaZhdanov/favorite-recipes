package com.github.nikita.zhdanov.favorite.recipes.service;

import com.github.nikita.zhdanov.favorite.recipes.error.RecipeNotExists;
import com.github.nikita.zhdanov.favorite.recipes.model.Recipe;
import com.github.nikita.zhdanov.favorite.recipes.model.RecipeFilters;
import com.github.nikita.zhdanov.favorite.recipes.repository.RecipeRepository;
import com.github.nikita.zhdanov.favorite.recipes.repository.RecipeSearchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class RecipeService {
    @Autowired
    private RecipeRepository recipeRepository;
    @Autowired
    private RecipeSearchRepository recipeSearchRepository;

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

    public Recipe get(String id) {
        return recipeRepository.findById(id).orElseThrow(() -> new RecipeNotExists(id));
    }

    public Collection<Recipe> search(RecipeFilters filters) {
        return recipeSearchRepository.find(filters);
    }
}
