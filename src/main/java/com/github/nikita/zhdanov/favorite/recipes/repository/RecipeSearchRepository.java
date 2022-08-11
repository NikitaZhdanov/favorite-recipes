package com.github.nikita.zhdanov.favorite.recipes.repository;

import com.github.nikita.zhdanov.favorite.recipes.model.Recipe;
import com.github.nikita.zhdanov.favorite.recipes.model.RecipeFilters;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.Collection;

@Repository
public class RecipeSearchRepository {
    @PersistenceContext
    private EntityManager entityManager;

    public Collection<Recipe> find(RecipeFilters filters) {
        var criteriaBuilder = entityManager.getCriteriaBuilder();
        var query = criteriaBuilder.createQuery(Recipe.class);
        var root = query.from(Recipe.class);
        var whereCauses = new ArrayList<Predicate>();

        addTextContainsCause(filters.getRecipeName(), "name", root, whereCauses);
        addTextContainsCause(filters.getInstructions(), "instructions", root, whereCauses);
        addIngredientsContainsCause(filters.getIngredientName(), whereCauses, root);
        addVegetarianCause(filters.getVegetarian(), whereCauses, root);

        query.select(root).where(whereCauses.toArray(new Predicate[0]));

        return entityManager.createQuery(query.select(root))
                .setFirstResult(filters.getPageNumber() * filters.getPageSize())
                .setMaxResults(filters.getPageSize())
                .getResultList();
    }

    private void addTextContainsCause(RecipeFilters.ContainsTextFilter filter, String fieldName, Root<Recipe> root,
                                      ArrayList<Predicate> whereCauses) {
        if (filter == null) {
            return;
        }
        var criteriaBuilder = entityManager.getCriteriaBuilder();
        var criteria = criteriaBuilder
                .lower(criteriaBuilder.literal("%" + filter.getContainsText().toLowerCase() + "%"));
        Path<String> path = root.get(fieldName);
        addToWhereCauses(criteriaBuilder, whereCauses, criteria, path, filter.isReverse());
    }

    private void addIngredientsContainsCause(RecipeFilters.ExactTextFilter filter, ArrayList<Predicate> whereCauses,
                                             Root<Recipe> recipeRoot) {
        if (filter == null) {
            return;
        }

        var criteriaBuilder = entityManager.getCriteriaBuilder();
        var ingredients = recipeRoot.join("ingredients", JoinType.LEFT);

        var lowerCriteria = criteriaBuilder.lower(criteriaBuilder.literal(filter.getExactText().toLowerCase()));
        var filterCriteria = criteriaBuilder.equal(ingredients.get("name"), lowerCriteria);
        ingredients.on(filterCriteria);

        if (filter.isReverse()) {
            whereCauses.add(criteriaBuilder.isNull(ingredients));
        } else {
            whereCauses.add(criteriaBuilder.isNotNull(ingredients));
        }
    }

    private void addVegetarianCause(Boolean vegetarian, ArrayList<Predicate> whereCauses, Root<Recipe> root) {
        if (vegetarian == null) {
            return;
        }
        var criteriaBuilder = entityManager.getCriteriaBuilder();
        var criteria = root.get("vegetarian");
        whereCauses.add(criteriaBuilder.equal(criteria, vegetarian));
    }

    private void addToWhereCauses(CriteriaBuilder criteriaBuilder, ArrayList<Predicate> whereCauses,
                                  Expression<String> criteria, Path<String> path, boolean reverse) {
        if (reverse) {
            whereCauses.add(criteriaBuilder.notLike(path, criteria));
        } else {
            whereCauses.add(criteriaBuilder.like(path, criteria));
        }
    }
}
