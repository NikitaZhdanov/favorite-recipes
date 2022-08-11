package com.github.nikita.zhdanov.favorite.recipes.integration;

import com.github.nikita.zhdanov.favorite.recipes.Application;
import com.github.nikita.zhdanov.favorite.recipes.model.Ingredient;
import com.github.nikita.zhdanov.favorite.recipes.model.Recipe;
import com.github.nikita.zhdanov.favorite.recipes.model.RecipeSearchResponse;
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
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.stream.StreamSupport;

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
                        Ingredient.builder().name("Cucumber").amount(500).build(),
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
                        Ingredient.builder().name("Cucumber").amount(500).build(),
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
    public void should_return_404_when_deleting_by_non_existing_recipe_id() {
        // given
        var recipe = Recipe.builder()
                .name("Salad with tomato, cucumber and onion")
                .ingredients(List.of(
                        Ingredient.builder().name("Tomato").amount(500).build(),
                        Ingredient.builder().name("Cucumber").amount(500).build(),
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

    @Test
    public void should_successfully_get_recipe_by_id() {
        // given
        var recipe = Recipe.builder()
                .name("Salad with tomato, cucumber and onion")
                .ingredients(List.of(
                        Ingredient.builder().name("Tomato").amount(500).build(),
                        Ingredient.builder().name("Cucumber").amount(500).build(),
                        Ingredient.builder().name("Onion").amount(100).build()
                ))
                .instructions("Cut the tomato, cucumber and onion into small pieces.\nMix the ingredients together in a bowl.\nAdd some salt.")
                .vegetarian(true)
                .build();
        recipeRepository.save(recipe);

        // when
        var retrievedRecipe = restTemplate.getForObject(URI.create("http://localhost:" + localServerPort + "/recipe/" + recipe.getId()), Recipe.class);

        assertThat(retrievedRecipe).isEqualTo(recipe);
    }

    @Test
    public void should_return_404_when_getting_by_non_existing_recipe_id() {
        // given
        var recipe = Recipe.builder()
                .name("Salad with tomato, cucumber and onion")
                .ingredients(List.of(
                        Ingredient.builder().name("Tomato").amount(500).build(),
                        Ingredient.builder().name("Cucumber").amount(500).build(),
                        Ingredient.builder().name("Onion").amount(100).build()
                ))
                .instructions("Cut the tomato, cucumber and onion into small pieces.\nMix the ingredients together in a bowl.\nAdd some salt.")
                .vegetarian(true)
                .build();
        recipeRepository.save(recipe);

        // when
        try {
            restTemplate.getForObject(URI.create("http://localhost:" + localServerPort + "/recipe/non-existing-id"), Recipe.class);
        } catch (HttpClientErrorException.NotFound e) {
            // then
            return;
        }
        throw new AssertionError("Expected BadRequest exception");
    }

    @Test
    public void should_search_recipes_by_name() {
        // given
        var existingRecipes = recipeRepository.saveAll(List.of(
                Recipe.builder()
                        .name("Pizza mozzarella")
                        .ingredients(List.of(
                                Ingredient.builder().name("Tomato").amount(500).build(),
                                Ingredient.builder().name("Mozzarella").amount(500).build(),
                                Ingredient.builder().name("Dough").amount(500).build()
                        ))
                        .instructions("Cut the tomato in small pieces. \nPrepare the dough and put the tomato pieces on it." +
                                "\nPut mozzarella on it.\nPut the pizza into over and cook for 20 minutes.")
                        .vegetarian(false)
                        .build(),
                Recipe.builder()
                        .name("Salad with tomato, cucumber and onion")
                        .ingredients(List.of(
                                Ingredient.builder().name("Tomato").amount(500).build(),
                                Ingredient.builder().name("Cucumber").amount(500).build(),
                                Ingredient.builder().name("Onion").amount(100).build()
                        ))
                        .instructions("Cut the tomato, cucumber and onion into small pieces.\nMix the ingredients together in a bowl.\nAdd some salt.")
                        .vegetarian(true)
                        .build()
        ));

        var filters = "{\n" +
                "  \"recipeName\": \n" +
                "    {\n" +
                "      \"containsText\": \"Pizza\"\n" +
                "    }\n" +
                "}";

        // when
        var recipes = restTemplate.postForObject(URI.create("http://localhost:" + localServerPort + "/recipe/search"),
                createHttpEntity(filters), RecipeSearchResponse.class);

        // then
        var pizzaMozzarella = StreamSupport.stream(existingRecipes.spliterator(), false)
                .filter(r -> r.getName().equals("Pizza mozzarella"))
                .findFirst().get();
        assertThat(recipes.getRecipes()).containsExactly(pizzaMozzarella);
        assertThat(recipes.getPageNumber()).isEqualTo(0);
        assertThat(recipes.getPageSize()).isEqualTo(100);
        assertThat(recipes.getCurrentNumberOfRecipes()).isEqualTo(1);
    }

    @Test
    public void should_search_recipes_by_name_and_ingredients() {
        // given
        var existingRecipes = recipeRepository.saveAll(List.of(
                Recipe.builder()
                        .name("Pizza mozzarella")
                        .ingredients(List.of(
                                Ingredient.builder().name("Tomato").amount(500).build(),
                                Ingredient.builder().name("Mozzarella").amount(500).build(),
                                Ingredient.builder().name("Dough").amount(500).build()
                        ))
                        .instructions("Cut the tomato in small pieces. \nPrepare the dough and put the tomato pieces on it." +
                                "\nPut mozzarella on it.\nPut the pizza into over and cook for 20 minutes.")
                        .vegetarian(false)
                        .build(),
                Recipe.builder()
                        .name("Tuna pizza")
                        .ingredients(List.of(
                                Ingredient.builder().name("Tomatoes").amount(500).build(),
                                Ingredient.builder().name("Tuna").amount(500).build(),
                                Ingredient.builder().name("Mozzarella").amount(500).build(),
                                Ingredient.builder().name("Dough").amount(100).build()
                        ))
                        .instructions("Cut the tomato, cucumber and onion into small pieces.\nMix the ingredients together in a bowl.\nAdd some salt.")
                        .vegetarian(false)
                        .build()
        ));

        var filters = "{\n" +
                "  \"recipeName\": {\n" +
                "      \"containsText\": \"Pizza\"\n" +
                "    },\n" +
                "  \"ingredientName\":\n" +
                "    {\n" +
                "      \"exactText\": \"tuna\",\n" +
                "      \"reverse\": true\n" +
                "    }\n" +
                "}";

        // when
        var recipes = restTemplate.postForObject(URI.create("http://localhost:" + localServerPort + "/recipe/search"),
                createHttpEntity(filters), RecipeSearchResponse.class);

        // then
        var pizzaMozzarella = StreamSupport.stream(existingRecipes.spliterator(), false)
                .filter(r -> r.getName().equals("Pizza mozzarella"))
                .findFirst().get();
        assertThat(recipes.getRecipes()).containsExactly(pizzaMozzarella);
        assertThat(recipes.getPageNumber()).isEqualTo(0);
        assertThat(recipes.getPageSize()).isEqualTo(100);
        assertThat(recipes.getCurrentNumberOfRecipes()).isEqualTo(1);
    }

    @Test
    public void should_search_recipes_by_ingredients() {
        // given
        var existingRecipes = recipeRepository.saveAll(List.of(
                Recipe.builder()
                        .name("Pizza mozzarella")
                        .ingredients(List.of(
                                Ingredient.builder().name("Tomato").amount(500).build(),
                                Ingredient.builder().name("Mozzarella").amount(500).build(),
                                Ingredient.builder().name("Dough").amount(500).build()
                        ))
                        .instructions("Cut the tomato in small pieces. \nPrepare the dough and put the tomato pieces on it." +
                                "\nPut mozzarella on it.\nPut the pizza into over and cook for 20 minutes.")
                        .vegetarian(false)
                        .build(),
                Recipe.builder()
                        .name("Tuna pizza")
                        .ingredients(List.of(
                                Ingredient.builder().name("Tomatoes").amount(500).build(),
                                Ingredient.builder().name("Tuna").amount(500).build(),
                                Ingredient.builder().name("Mozzarella").amount(500).build(),
                                Ingredient.builder().name("Dough").amount(100).build()
                        ))
                        .instructions("Cut the tomato, cucumber and onion into small pieces.\nMix the ingredients together in a bowl.\nAdd some salt.")
                        .vegetarian(false)
                        .build()
        ));

        var filters = "{\n" +
                "  \"recipeName\": {\n" +
                "      \"containsText\": \"Pizza\"\n" +
                "    },\n" +
                "  \"ingredientName\":\n" +
                "    {\n" +
                "      \"exactText\": \"mozzarella\"\n" +
                "    }\n" +
                "}";

        // when
        var recipes = restTemplate.postForObject(URI.create("http://localhost:" + localServerPort + "/recipe/search"),
                createHttpEntity(filters), RecipeSearchResponse.class);

        // then
        var expectedPizzas = StreamSupport.stream(existingRecipes.spliterator(), false).toArray(Recipe[]::new);
        assertThat(recipes.getRecipes()).contains(expectedPizzas[0], expectedPizzas[1]);
        assertThat(recipes.getPageNumber()).isEqualTo(0);
        assertThat(recipes.getPageSize()).isEqualTo(100);
    }

    @Test
    public void should_search_by_instructions_and_vegetarian() {
        // given
        var existingRecipes = recipeRepository.saveAll(List.of(
                Recipe.builder()
                        .name("Pizza mozzarella")
                        .ingredients(List.of(
                                Ingredient.builder().name("Tomato").amount(500).build(),
                                Ingredient.builder().name("Mozzarella").amount(500).build(),
                                Ingredient.builder().name("Dough").amount(500).build()
                        ))
                        .instructions("Cut the tomato in small pieces. \nPrepare the dough and put the tomato pieces on it." +
                                "\nPut mozzarella on it.\nPut the pizza into over and cook for 20 minutes.")
                        .vegetarian(false)
                        .build(),
                Recipe.builder()
                        .name("Salad with tomatoes and cucumber")
                        .ingredients(List.of(
                                Ingredient.builder().name("Tomatoes").amount(500).build(),
                                Ingredient.builder().name("Cucumber").amount(500).build(),
                                Ingredient.builder().name("Rucola").amount(500).build()
                        ))
                        .instructions("Cut the tomato, cucumber and onion into small pieces.\nMix the ingredients together in a bowl.\nAdd some salt.")
                        .vegetarian(true)
                        .build()
        ));

        var filters = "{\n" +
                "  \"instructions\": {\n" +
                "      \"containsText\": \"bowl\"\n" +
                "    },\n" +
                "  \"vegetarian\": true\n" +
                "}";

        // when
        var recipes = restTemplate.postForObject(URI.create("http://localhost:" + localServerPort + "/recipe/search"),
                createHttpEntity(filters), RecipeSearchResponse.class);

        // then
        var salad = StreamSupport.stream(existingRecipes.spliterator(), false)
                .filter(r -> r.getName().equals("Salad with tomatoes and cucumber"))
                .findFirst().get();
        assertThat(recipes.getRecipes()).containsExactly(salad);
        assertThat(recipes.getPageNumber()).isEqualTo(0);
        assertThat(recipes.getPageSize()).isEqualTo(100);
    }

    @Test
    public void should_return_all_recipes_when_search_body_is_null() {
        // given
        var existingRecipes = recipeRepository.saveAll(List.of(
                Recipe.builder()
                        .name("Pizza mozzarella")
                        .ingredients(List.of(
                                Ingredient.builder().name("Tomato").amount(500).build(),
                                Ingredient.builder().name("Mozzarella").amount(500).build(),
                                Ingredient.builder().name("Dough").amount(500).build()
                        ))
                        .instructions("Cut the tomato in small pieces. \nPrepare the dough and put the tomato pieces on it." +
                                "\nPut mozzarella on it.\nPut the pizza into over and cook for 20 minutes.")
                        .vegetarian(false)
                        .build(),
                Recipe.builder()
                        .name("Tuna pizza")
                        .ingredients(List.of(
                                Ingredient.builder().name("Tomatoes").amount(500).build(),
                                Ingredient.builder().name("Tuna").amount(500).build(),
                                Ingredient.builder().name("Mozzarella").amount(500).build(),
                                Ingredient.builder().name("Dough").amount(100).build()
                        ))
                        .instructions("Cut the tomato, cucumber and onion into small pieces.\nMix the ingredients together in a bowl.\nAdd some salt.")
                        .vegetarian(false)
                        .build()
        ));

        // when
        var recipes = restTemplate.postForObject(URI.create("http://localhost:" + localServerPort + "/recipe/search"),
                createHttpEntity(null), RecipeSearchResponse.class);

        // then
        var expectedPizzas = StreamSupport.stream(existingRecipes.spliterator(), false).toArray(Recipe[]::new);
        assertThat(recipes.getRecipes()).contains(expectedPizzas[0], expectedPizzas[1]);
        assertThat(recipes.getPageNumber()).isEqualTo(0);
        assertThat(recipes.getPageSize()).isEqualTo(100);
    }

    private static HttpEntity<String> createHttpEntity(String body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }
}
