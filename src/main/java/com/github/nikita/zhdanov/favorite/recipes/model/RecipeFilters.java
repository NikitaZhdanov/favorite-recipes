package com.github.nikita.zhdanov.favorite.recipes.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("Filters to search recipes.")
public class RecipeFilters {
    @ApiModelProperty(value = "Recipe name contains this string", example = "Pizza")
    @Valid
    private ContainsTextFilter recipeName;

    @ApiModelProperty(value = "Recipe instructions contain this string", example = "Oven")
    @Valid
    private ContainsTextFilter instructions;

    @ApiModelProperty(value = "Recipe has specific ingredients", example = "Tomatoes")
    @Valid
    private ExactTextFilter ingredientName;

    @ApiModelProperty(value = "Recipe is vegetarian", example = "true")
    private Boolean vegetarian;

    @ApiModelProperty(value = "Pagination: page number", example = "0")
    @Min(value = 0, message = "Page number must be greater than or equal to 0")
    private int pageNumber;

    @ApiModelProperty(value = "Pagination: size of the page. Max = 100000, default = 100", example = "10", required = true)
    @Max(value = 100000, message = "Max value for pageSize is 100000")
    @Min(value = 1, message = "Min value for pageSize is 1")
    private Integer pageSize;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @EqualsAndHashCode(callSuper = true)
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContainsTextFilter extends Filter {
        @ApiModelProperty(value = "If text contains this word (case-insensitive)", example = "Pizza")
        @NotBlank(message = "Filter text must not be blank")
        private String containsText;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @EqualsAndHashCode(callSuper = true)
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExactTextFilter extends Filter {
        @ApiModelProperty(value = "If text is exactly this word (case-insensitive)", example = "Tomatoes")
        @NotBlank(message = "Filter text must not be blank")
        private String exactText;
    }

    @Getter
    @Setter
    public static abstract class Filter {
        @ApiModelProperty(value = "Reverses the filter. For example: if reverse is true and the text is \"salmon\"" +
                " then the search will exclude the recipes with salmon from the result", example = "false")
        private boolean reverse;
    }
}
