package com.yazlab;

import java.util.List;
import java.util.ArrayList;

public class Recipe {
    private int id;
    private String name;
    private String category;
    private int preparationTime;
    private String instructions;
    private String ingredientList;
    private List<RecipeIngredient> recipeIngredients;
    private double totalCost;

    public List<RecipeIngredient> getRecipeIngredients() {
        return recipeIngredients;
    }

    public Recipe() {
        this.recipeIngredients = new ArrayList<>();
    }

    public Recipe(List<RecipeIngredient> recipeIngredients) {
        this.recipeIngredients = recipeIngredients != null ? recipeIngredients : new ArrayList<>();
    }

    public void setRecipeIngredients(List<RecipeIngredient> recipeIngredients) {
        this.recipeIngredients = recipeIngredients != null ? recipeIngredients : new ArrayList<>();
    }

    public Recipe(int id, String name, String category, int preparationTime, String instructions, String ingredientList) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.preparationTime = preparationTime;
        this.instructions = instructions;
        this.ingredientList = ingredientList;
        this.totalCost = 0.0;
    }

    public double getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(double totalCost) {
        this.totalCost = totalCost;
    }

    public String getIngredientList() {
        return ingredientList;
    }

    public void setIngredientList(String ingredientList) {
        this.ingredientList = ingredientList;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getPreparationTime() {
        return preparationTime;
    }

    public void setPreparationTime(int preparationTime) {
        this.preparationTime = preparationTime;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}