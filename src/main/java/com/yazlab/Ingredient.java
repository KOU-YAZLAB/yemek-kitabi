package com.yazlab;

import javafx.beans.property.*;

public class Ingredient {
    private final IntegerProperty ingredientId;
    private final StringProperty ingredientName;
    private final StringProperty totalAmount;
    private final StringProperty unit;
    private final DoubleProperty unitPrice;

    public Ingredient(int ingredientId, String ingredientName, String totalAmount, String unit, double unitPrice) {
        this.ingredientId = new SimpleIntegerProperty(ingredientId);
        this.ingredientName = new SimpleStringProperty(ingredientName);
        this.totalAmount = new SimpleStringProperty(totalAmount);
        this.unit = new SimpleStringProperty(unit);
        this.unitPrice = new SimpleDoubleProperty(unitPrice);
    }

    // Getter ve setter metodları
    public int getIngredientId() {
        return ingredientId.get();
    }

    public void setIngredientId(int ingredientId) {
        this.ingredientId.set(ingredientId);
    }

    public IntegerProperty ingredientIdProperty() {
        return ingredientId;
    }

    @Override
    public String toString() {
        return getIngredientName();
    }

    public String getIngredientName() {
        return ingredientName.get();
    }

    public void setIngredientName(String ingredientName) {
        this.ingredientName.set(ingredientName);
    }

    public StringProperty ingredientNameProperty() {
        return ingredientName;
    }

    public String getTotalAmount() {
        return totalAmount.get();
    }

    public void setTotalAmount(String totalAmount) {
        this.totalAmount.set(totalAmount);
    }

    public StringProperty totalAmountProperty() {
        return totalAmount;
    }

    public String getUnit() {
        return unit.get();
    }

    public void setUnit(String unit) {
        this.unit.set(unit);
    }

    public StringProperty unitProperty() {
        return unit;
    }

    public double getUnitPrice() {
        return unitPrice.get();
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice.set(unitPrice);
    }

    public DoubleProperty unitPriceProperty() {
        return unitPrice;
    }
}
