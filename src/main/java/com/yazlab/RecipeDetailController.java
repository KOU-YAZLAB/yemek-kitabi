package com.yazlab;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RecipeDetailController {
    @FXML
    private TextField recipeNameField;
    @FXML
    private TextField categoryField;
    @FXML
    private TextField preparationTimeField;
    @FXML
    private TextArea instructionsArea;
    @FXML
    private TableView<RecipeIngredient> editRecipeIngredientTable;
    @FXML
    private TableColumn<RecipeIngredient, String> editIngredientNameColumn;
    @FXML
    private TableColumn<RecipeIngredient, Float> editIngredientAmountColumn;
    @FXML
    private TableColumn<RecipeIngredient, String> editIngredientUnitColumn;

    private ObservableList<RecipeIngredient> editRecipeIngredients = FXCollections.observableArrayList();

    private Recipe currentRecipe;

    public void initialize() {
        editIngredientNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        editIngredientAmountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        editIngredientUnitColumn.setCellValueFactory(new PropertyValueFactory<>("unit"));
        editRecipeIngredientTable.setItems(editRecipeIngredients);
    }

    public void setRecipe(Recipe recipe) {
        this.currentRecipe = recipe;
        loadRecipeIngredients(recipe.getId());
        recipeNameField.setText(recipe.getName());
        categoryField.setText(recipe.getCategory());
        preparationTimeField.setText(String.valueOf(recipe.getPreparationTime()));
        instructionsArea.setText(recipe.getInstructions());
    }

    private void loadRecipeIngredients(int recipeId) {
        editRecipeIngredients.clear();
        String query = "SELECT m.MalzemeAdi, rm.MalzemeMiktar, m.MalzemeBirim FROM TarifMalzeme rm " +
                "JOIN Malzemeler m ON rm.MalzemeID = m.MalzemeID WHERE rm.TarifID = ?";

        try (Connection conn = DbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, recipeId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String name = rs.getString("MalzemeAdi");
                float amount = rs.getFloat("MalzemeMiktar");
                String unit = rs.getString("MalzemeBirim");

                RecipeIngredient recipeIngredient = new RecipeIngredient(0, name, amount, unit);
                editRecipeIngredients.add(recipeIngredient);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Tarif malzemeleri yüklenirken hata oluştu.");
        }
    }

    @FXML
    private void updateRecipe() {
        currentRecipe.setName(recipeNameField.getText());
        currentRecipe.setCategory(categoryField.getText());
        currentRecipe.setPreparationTime(Integer.parseInt(preparationTimeField.getText()));
        currentRecipe.setInstructions(instructionsArea.getText());
        DbConnection.updateRecipe(currentRecipe);
        Stage stage = (Stage) recipeNameField.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void deleteRecipe() {

        if (currentRecipe == null) {
            System.out.println("Lütfen bir tarif seçin.");
            return;
        }

        int recipeId = currentRecipe.getId();
        String query = "DELETE FROM Tarifler WHERE TarifID = ?";

        try (Connection conn = DbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, recipeId);
            pstmt.executeUpdate();
            System.out.println("Tarif başarıyla silindi.");

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Başarılı");
            alert.setHeaderText(null);
            alert.setContentText("Tarif başarıyla silindi.");
            alert.showAndWait();

            Stage stage = (Stage) recipeNameField.getScene().getWindow();
            stage.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Tarif silinirken bir hata oluştu.");
        }
    }
}