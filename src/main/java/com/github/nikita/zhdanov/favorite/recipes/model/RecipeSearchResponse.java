package com.github.nikita.zhdanov.favorite.recipes.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("Result of the recipe search")
public class RecipeSearchResponse {
    @ApiModelProperty(value = "Found recipes")
    private Collection<Recipe> recipes;
    @ApiModelProperty(value = "Pagination information: the page size used for the search." +
            "If the currentNumberOfRecipes contains exactly this number of recipes, need to make another search with the next page number.")
    private int pageSize;
    @ApiModelProperty(value = "Pagination information: current page number.")
    private int pageNumber;
    @ApiModelProperty(value = "Pagination information: current number of recipes found." +
            "If this number is equal to the page size, need to make another search with the next page number.")
    private int currentNumberOfRecipes;
}
