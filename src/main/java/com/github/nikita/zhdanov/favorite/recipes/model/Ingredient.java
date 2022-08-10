package com.github.nikita.zhdanov.favorite.recipes.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ingredients")
public class Ingredient {
    @Id
    @ApiParam(value = "Unique identifier of the ingredient", example = "12345678-1234-1234-1234-1234567890ab")
    private String id = UUID.randomUUID().toString();

    @NotBlank(message = "Name is required")
    @ApiParam(value = "Name of the ingredient", example = "Tomatoes")
    private String name;

    @ApiParam(value = "Amount of the ingredient in grams", example = "100")
    private int amount;
}
