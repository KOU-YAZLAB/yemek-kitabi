package com.yazlab;

public class RecipeIngredient {
    private final int ingredientId;
    private final String name;
    private final float amount;
    private final String unit;
    private float totalAvailableAmount;

    public float getTotalAvailableAmount() {
        return totalAvailableAmount;
    }

    public void setTotalAvailableAmount(float totalAvailableAmount) {
        this.totalAvailableAmount = totalAvailableAmount;
    }

    public RecipeIngredient(int ingredientId, String name, float amount, String unit) {
        this.ingredientId = ingredientId;
        this.name = name;
        this.amount = amount;
        this.unit = unit;
    }

    public int getIngredientId() {
        return ingredientId;
    }

    public String getName() {
        return name;
    }

    public float getAmount() {
        return amount;
    }

    public String getUnit() {
        return unit;
    }
}
