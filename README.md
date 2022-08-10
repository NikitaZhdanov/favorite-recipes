# favorite-recipes

## Description

Service which allows users to manage their favourite recipes. It allows adding, updating, removing and fetching recipes.
Additionally, users are able to filter available recipes based on one or more of the following criteria:
1. Whether the dish is vegetarian
2. The number of servings
3. Specific ingredients (either include or exclude)
4. Text search within the instructions.

## Prerequisites

* [Java 11+](https://www.java.com/en/download/)
* [Maven](https://maven.apache.org/)
* [MariaDB](https://mariadb.org/)

## Installation

```bash
mvn clean install
```

## Usage

1. MariaDB must be running.
2. Copy the `src/main/resources/application.yml` file to the working directory.
3. Configure the copied application.yml file following the comments in the file.
4. Run the application:

```bash
java -jar target/favorite-recipes.jar
```

## Documentation

* [README.md](README.md)
* Swagger: http://localhost:8080/swagger-ui/
