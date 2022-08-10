package com.github.nikita.zhdanov.favorite.recipes.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.Collection;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "recipes")
@ApiModel("Recipe")
public class Recipe {
    @Id
    @ApiModelProperty(value = "Unique identifier of the recipe", example = "12345678-1234-1234-1234-1234567890ab")
    private String id = UUID.randomUUID().toString();

    @NotBlank(message = "Name is required")
    @ApiModelProperty(value = "Name of the recipe", example = "Pizza margarita")
    private String name;

    @NotBlank(message = "Instructions are required")
    @ApiModelProperty(value = "Instructions for the recipe",
            example = "1. Put the pizza in the oven\n2. Put the pizza in the oven\n3. Put the pizza in the oven")
    private String instructions;

    @ManyToMany(cascade = CascadeType.ALL)
    @NotEmpty(message = "Ingredients are required")
    @ApiModelProperty(value = "Ingredients for the recipe",
            example = "[{\"name\":\"Tomatoes\",\"amount\":100},{\"name\":\"Mozzarella\",\"amount\":100}]")
    private Collection<Ingredient> ingredients;

    @ApiModelProperty(value = "Is the dish vegetarian?", example = "true")
    private boolean vegetarian;
}
