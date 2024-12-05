package com.yazlab;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AddIngredientController {

    private final ObservableList<Ingredient> ingredientList = FXCollections.observableArrayList();
    @FXML
    private TextField ingredientNameField;
    @FXML
    private TextField totalAmountField;
    @FXML
    private TextField unitField;
    @FXML
    private TextField unitPriceField;
    @FXML
    private TableView<Ingredient> ingredientTable;
    @FXML
    private TableColumn<Ingredient, Integer> ingredientIdColumn;
    @FXML
    private TableColumn<Ingredient, String> ingredientNameColumn;
    @FXML
    private TableColumn<Ingredient, String> totalAmountColumn;
    @FXML
    private TableColumn<Ingredient, String> unitColumn;
    @FXML
    private TableColumn<Ingredient, Double> unitPriceColumn;

    @FXML
    public void initialize() {
        ingredientIdColumn.setCellValueFactory(new PropertyValueFactory<>("ingredientId"));
        ingredientNameColumn.setCellValueFactory(new PropertyValueFactory<>("ingredientName"));
        totalAmountColumn.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        unitColumn.setCellValueFactory(new PropertyValueFactory<>("unit"));
        unitPriceColumn.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));

        loadIngredients();
    }

    private void loadIngredients() {
        ingredientList.clear();

        try (Connection conn = DbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM Malzemeler");
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("MalzemeID");
                String name = rs.getString("MalzemeAdi");
                String totalAmount = rs.getString("ToplamMiktar");
                String unit = rs.getString("MalzemeBirim");
                double unitPrice = rs.getDouble("BirimFiyat");

                Ingredient ingredient = new Ingredient(id, name, totalAmount, unit, unitPrice);
                ingredientList.add(ingredient);
            }
            ingredientTable.setItems(ingredientList);

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Malzemeler yüklenirken bir hata oluştu.");
        }
    }

    @FXML
    private void addNewIngredient() {
        String name = ingredientNameField.getText();
        String totalAmount = totalAmountField.getText();
        String unit = unitField.getText();
        double unitPrice = Double.parseDouble(unitPriceField.getText());

        try (Connection conn = DbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO Malzemeler (MalzemeAdi, ToplamMiktar, MalzemeBirim, BirimFiyat) VALUES (?, ?, ?, ?)")) {

            stmt.setString(1, name);
            stmt.setString(2, totalAmount);
            stmt.setString(3, unit);
            stmt.setDouble(4, unitPrice);
            stmt.executeUpdate();

            loadIngredients();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Malzeme eklenirken bir hata oluştu.");
        }
    }

    @FXML
    private void editSelectedIngredient() {
        Ingredient selectedIngredient = ingredientTable.getSelectionModel().getSelectedItem();

        if (selectedIngredient == null) {
            System.out.println("Lütfen bir malzeme seçin.");
            return;
        }

        ingredientNameField.setText(selectedIngredient.getIngredientName());
        totalAmountField.setText(selectedIngredient.getTotalAmount());
        unitField.setText(selectedIngredient.getUnit());
        unitPriceField.setText(String.valueOf(selectedIngredient.getUnitPrice()));
    }

    @FXML
    private void updateIngredient() {
        Ingredient selectedIngredient = ingredientTable.getSelectionModel().getSelectedItem();

        if (selectedIngredient == null) {
            System.out.println("Lütfen bir malzeme seçin.");
            return;
        }

        selectedIngredient.setIngredientName(ingredientNameField.getText());
        selectedIngredient.setTotalAmount(totalAmountField.getText());
        selectedIngredient.setUnit(unitField.getText());

        try {
            double unitPrice = Double.parseDouble(unitPriceField.getText());
            selectedIngredient.setUnitPrice(unitPrice);
        } catch (NumberFormatException e) {
            System.out.println("Geçersiz birim fiyat değeri.");
        }

        saveIngredientChangesToDatabase(selectedIngredient);
        ingredientTable.refresh();
    }

    private void saveIngredientChangesToDatabase(Ingredient ingredient) {
        String sql = "UPDATE Malzemeler SET MalzemeAdi = ?, ToplamMiktar = ?, MalzemeBirim = ?, BirimFiyat = ? WHERE MalzemeID = ?";

        try (Connection conn = DbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, ingredient.getIngredientName());
            pstmt.setString(2, ingredient.getTotalAmount());
            pstmt.setString(3, ingredient.getUnit());
            pstmt.setDouble(4, ingredient.getUnitPrice());
            pstmt.setInt(5, ingredient.getIngredientId());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Veritabanı güncellenirken bir hata oluştu.");
        }
    }

    @FXML
    private void deleteSelectedIngredient() {
        Ingredient selectedIngredient = ingredientTable.getSelectionModel().getSelectedItem();
        if (selectedIngredient != null) {
            try (Connection conn = DbConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("DELETE FROM Malzemeler WHERE MalzemeID = ?")) {

                stmt.setInt(1, selectedIngredient.getIngredientId());
                stmt.executeUpdate();
                ingredientList.remove(selectedIngredient);
            } catch (SQLException e) {
                e.printStackTrace();
                System.out.println("Malzeme silinirken bir hata oluştu.");
            }
        } else {
            System.out.println("Silmek için bir malzeme seçin.");
        }
    }
}
