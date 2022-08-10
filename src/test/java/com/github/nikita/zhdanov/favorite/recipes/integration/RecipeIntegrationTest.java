package com.github.nikita.zhdanov.favorite.recipes.integration;

import com.github.nikita.zhdanov.favorite.recipes.Application;
import com.github.nikita.zhdanov.favorite.recipes.model.Ingredient;
import com.github.nikita.zhdanov.favorite.recipes.model.Recipe;
import com.github.nikita.zhdanov.favorite.recipes.repository.IngredientRepository;
import com.github.nikita.zhdanov.favorite.recipes.repository.RecipeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {EmbeddedMariaDbConfig.class, Application.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration
public class RecipeIntegrationTest {
    private final RestTemplate restTemplate = new RestTemplate();

    @LocalServerPort
    private int localServerPort;

    @Autowired
    private RecipeRepository recipeRepository;
    @Autowired
    private IngredientRepository ingredientRepository;

    @BeforeEach
    public void setUp() {
        recipeRepository.deleteAll();
    }

    @Test
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void should_successfully_add_recipe() {
        // given
        var recipeId = UUID.randomUUID().toString();
        var request = "{\n" +
                "  \"name\": \"Salad with tomato, cucumber and onion\",\n" +
                "  \"ingredients\": [\n" +
                "    {\n" +
                "      \"name\": \"Tomato\",\n" +
                "      \"amount\": \"500\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\": \"Cucumber\",\n" +
                "      \"amount\": \"100\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\": \"Onion\",\n" +
                "      \"amount\": \"500\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"instructions\": \"Cut the tomato, cucumber and onion into small pieces.\\nMix the ingredients together in a bowl.\\nAdd some salt.\",\n" +
                "  \"vegetarian\": true\n" +
                "}\n";

        // when
        restTemplate.put(URI.create("http://localhost:" + localServerPort + "/recipe/" + recipeId), createHttpEntity(request));

        // then
        var persistedRecipe = recipeRepository.findById(recipeId).orElseThrow();
        assertThat(persistedRecipe.getName()).isEqualTo("Salad with tomato, cucumber and onion");
        assertThat(persistedRecipe.getInstructions()).isEqualTo("Cut the tomato, cucumber and onion into small pieces.\nMix the ingredients together in a bowl.\nAdd some salt.");
        assertThat(persistedRecipe.isVegetarian()).isTrue();
        assertThat(persistedRecipe.getIngredients()).hasSize(3);
        assertThat(persistedRecipe.getIngredients()).extracting(Ingredient::getName).contains("Tomato", "Cucumber", "Onion");
        assertThat(persistedRecipe.getIngredients()).extracting(Ingredient::getAmount).contains(500, 100);
    }

    @Test
    public void should_not_add_recipe_without_id() {
        // given
        var request = "{\n" +
                "  \"name\": \"Salad with tomato, cucumber and onion\",\n" +
                "  \"ingredients\": [\n" +
                "    {\n" +
                "      \"name\": \"Tomato\",\n" +
                "      \"amount\": \"500\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\": \"Cucumber\",\n" +
                "      \"amount\": \"100\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\": \"Onion\",\n" +
                "      \"amount\": \"500\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"instructions\": \"Cut the tomato, cucumber and onion into small pieces.\\nMix the ingredients together in a bowl.\\nAdd some salt.\",\n" +
                "  \"vegetarian\": true\n" +
                "}\n";

        // when
        try {
            restTemplate.put(URI.create("http://localhost:" + localServerPort + "/recipe/"), createHttpEntity(request));
        } catch (HttpClientErrorException.NotFound e) {
            // then
            return;
        }
        throw new AssertionError("Expected BadRequest exception");
    }

    @Test
    public void should_not_add_recipe_with_empty_name() {
        // given
        var recipeId = UUID.randomUUID().toString();
        var request = "{\n" +
                "  \"ingredients\": [\n" +
                "    {\n" +
                "      \"name\": \"Tomato\",\n" +
                "      \"amount\": \"500\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\": \"Cucumber\",\n" +
                "      \"amount\": \"100\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\": \"Onion\",\n" +
                "      \"amount\": \"500\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"instructions\": \"Cut the tomato, cucumber and onion into small pieces.\\nMix the ingredients together in a bowl.\\nAdd some salt.\",\n" +
                "  \"vegetarian\": true\n" +
                "}\n";

        // when
        try {
            restTemplate.put(URI.create("http://localhost:" + localServerPort + "/recipe/" + recipeId), createHttpEntity(request));
        } catch (HttpClientErrorException.BadRequest e) {
            // then
            return;
        }

        throw new RuntimeException("Expected exception");
    }

    @Test
    public void should_not_add_recipe_without_ingredients() {
        // given
        var recipeId = UUID.randomUUID().toString();
        var request = "{\n" +
                "  \"name\": \"Salad with tomato, cucumber and onion\",\n" +
                "  \"instructions\": \"Cut the tomato, cucumber and onion into small pieces.\\nMix the ingredients together in a bowl.\\nAdd some salt.\",\n" +
                "  \"vegetarian\": true\n" +
                "}\n";

        // when
        try {
            restTemplate.put(URI.create("http://localhost:" + localServerPort + "/recipe/" + recipeId), createHttpEntity(request));
        } catch (HttpClientErrorException.BadRequest e) {
            // then
            return;
        }

        throw new RuntimeException("Expected exception");
    }

    @Test
    public void should_successfully_remove_recipe() {
        // given
        var recipe = Recipe.builder()
                .name("Salad with tomato, cucumber and onion")
                .ingredients(List.of(
                        Ingredient.builder().name("Tomato").amount(500).build(),
                        Ingredient.builder().name("Cucomber").amount(500).build(),
                        Ingredient.builder().name("Onion").amount(100).build()
                ))
                .instructions("Cut the tomato, cucumber and onion into small pieces.\nMix the ingredients together in a bowl.\nAdd some salt.")
                .vegetarian(true)
                .build();
        recipeRepository.save(recipe);

        // when
        restTemplate.delete(URI.create("http://localhost:" + localServerPort + "/recipe/" + recipe.getId()));

        // then
        recipeRepository.findById(recipe.getId()).ifPresent(
                persistedRecipe -> {
                    throw new RuntimeException("Expected recipe to be removed");
                }
        );
        ingredientRepository.findAll().forEach(
                ingredient -> {
                    throw new RuntimeException("Expected ingredient to be removed");
                }
        );
    }

    @Test
    public void should_nod_delete_recipe_without_id() {
        // given
        var recipe = Recipe.builder()
                .name("Salad with tomato, cucumber and onion")
                .ingredients(List.of(
                        Ingredient.builder().name("Tomato").amount(500).build(),
                        Ingredient.builder().name("Cucomber").amount(500).build(),
                        Ingredient.builder().name("Onion").amount(100).build()
                ))
                .instructions("Cut the tomato, cucumber and onion into small pieces.\nMix the ingredients together in a bowl.\nAdd some salt.")
                .vegetarian(true)
                .build();
        recipeRepository.save(recipe);

        // when
        try {
            restTemplate.delete(URI.create("http://localhost:" + localServerPort + "/recipe/"));
        } catch (HttpClientErrorException.NotFound e) {
            // then
            return;
        }
        throw new AssertionError("Expected BadRequest exception");
    }

    @Test
    public void should_return_404_for_non_existing_recipe_id() {
        // given
        var recipe = Recipe.builder()
                .name("Salad with tomato, cucumber and onion")
                .ingredients(List.of(
                        Ingredient.builder().name("Tomato").amount(500).build(),
                        Ingredient.builder().name("Cucomber").amount(500).build(),
                        Ingredient.builder().name("Onion").amount(100).build()
                ))
                .instructions("Cut the tomato, cucumber and onion into small pieces.\nMix the ingredients together in a bowl.\nAdd some salt.")
                .vegetarian(true)
                .build();
        recipeRepository.save(recipe);

        // when
        try {
            restTemplate.delete(URI.create("http://localhost:" + localServerPort + "/recipe/non-existing-id"));
        } catch (HttpClientErrorException.NotFound e) {
            // then
            return;
        }
        throw new AssertionError("Expected BadRequest exception");
    }

    private static HttpEntity<String> createHttpEntity(String body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }
}
