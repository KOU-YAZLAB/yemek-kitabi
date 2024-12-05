package com.yazlab;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MainController {
    private final ObservableList<RecipeIngredient> recipeIngredients = FXCollections.observableArrayList();
    private final ObservableList<Ingredient> addedIngredients = FXCollections.observableArrayList();
    private final ObservableList<Recipe> recipeList = FXCollections.observableArrayList();
    // Tarifler
    @FXML
    public TextField searchField;
    @FXML
    private TableView<Recipe> recipeTable;
    @FXML
    private TableColumn<Recipe, Integer> recipeIdColumn;
    @FXML
    private TableColumn<Recipe, String> recipeNameColumn;
    @FXML
    private TableColumn<Recipe, String> categoryColumn;
    @FXML
    private TableColumn<Recipe, Integer> preparationTimeColumn;
    @FXML
    private TableColumn<Recipe, String> instructionsColumn;
    @FXML
    private TableColumn<Recipe, String> ingredientListColumn;
    @FXML
    private TableColumn<Recipe, Double> totalCostColumn;
    // Tarif Ekleme
    @FXML
    private TextField recipeNameField;
    @FXML
    private TextField categoryField;
    @FXML
    private TextField preparationTimeField;
    @FXML
    private TextArea instructionsArea;
    // Malzeme Ekleme
    @FXML
    private ComboBox<Ingredient> ingredientComboBox;
    @FXML
    private TextField ingredientAmountField;
    @FXML
    private TableView<RecipeIngredient> recipeIngredientTable;
    @FXML
    private TableColumn<RecipeIngredient, String> ingredientNameColumn;
    @FXML
    private TableColumn<RecipeIngredient, Float> ingredientAmountColumn;
    @FXML
    private TableColumn<RecipeIngredient, String> ingredientUnitColumn;

    @FXML
    public void initialize() {
        recipeIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        recipeNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        preparationTimeColumn.setCellValueFactory(new PropertyValueFactory<>("preparationTime"));
        instructionsColumn.setCellValueFactory(new PropertyValueFactory<>("instructions"));
        ingredientListColumn.setCellValueFactory(new PropertyValueFactory<>("ingredientList"));
        totalCostColumn.setCellValueFactory(new PropertyValueFactory<>("totalCost"));

        ingredientNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        ingredientAmountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        ingredientUnitColumn.setCellValueFactory(new PropertyValueFactory<>("unit"));
        recipeIngredientTable.setItems(recipeIngredients);

        ingredientComboBox.setCellFactory(comboBox -> new ListCell<>() {
            @Override
            protected void updateItem(Ingredient item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getIngredientName());
                }
            }
        });

        ingredientComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Ingredient item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getIngredientName());
                }
            }
        });

        loadIngredients();
        loadRecipes();
    }

    @FXML
    private void loadRecipes() {
        recipeTable.getItems().clear();

        String query = "SELECT t.TarifID, t.TarifAdi, t.Kategori, t.HazirlamaSuresi, t.Talimatlar, " +
                "COALESCE(SUM(tm.MalzemeMiktar * m.BirimFiyat), 0) AS ToplamMaliyet " +
                "FROM Tarifler t " +
                "LEFT JOIN TarifMalzeme tm ON t.TarifID = tm.TarifID " +
                "LEFT JOIN Malzemeler m ON tm.MalzemeID = m.MalzemeID " +
                "GROUP BY t.TarifID, t.TarifAdi, t.Kategori, t.HazirlamaSuresi, t.Talimatlar;";

        try (Connection conn = DbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                int id = rs.getInt("TarifID");
                String name = rs.getString("TarifAdi");
                String category = rs.getString("Kategori");
                int preparationTime = rs.getInt("HazirlamaSuresi");
                String instructions = rs.getString("Talimatlar");
                double totalCost = rs.getDouble("ToplamMaliyet");

                String ingredientList = getIngredientsForRecipe(id);
                List<RecipeIngredient> ingredients = getRecipeIngredients(id);
                Recipe recipe = new Recipe(id, name, category, preparationTime, instructions, ingredientList);
                recipe.setTotalCost(totalCost);
                recipe.setRecipeIngredients(ingredients);
                recipeList.add(recipe);
            }
            recipeTable.setItems(recipeList);

            // RowFactory ile malzeme durumu kontrolü
            recipeTable.setRowFactory(tv -> new TableRow<Recipe>() {
                @Override
                protected void updateItem(Recipe item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setStyle("");
                    } else {
                        boolean hasSufficientIngredients = true;
                        for (RecipeIngredient ingredient : item.getRecipeIngredients()) {
                            if (ingredient.getAmount() > ingredient.getTotalAvailableAmount()) {
                                hasSufficientIngredients = false;
                                break;
                            }
                        }
                        if (hasSufficientIngredients) {
                            setStyle("-fx-background-color: green;");
                        } else {
                            setStyle("-fx-background-color: red;");
                        }
                    }
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Veritabanından tarifler çekilirken bir hata oluştu.");
        }
    }

    private List<RecipeIngredient> getRecipeIngredients(int recipeId) {
        List<RecipeIngredient> ingredients = new ArrayList<>();
        String query = "SELECT MalzemeAdi, MalzemeMiktar, ToplamMiktar FROM TarifMalzeme ri JOIN Malzemeler m ON ri.MalzemeID = m.MalzemeID WHERE ri.TarifID = ?";

        try (Connection conn = DbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, recipeId);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String ingredientName = rs.getString("MalzemeAdi");
                double amount = rs.getDouble("MalzemeMiktar");
                double totalAvailableAmount = rs.getDouble("ToplamMiktar");
                RecipeIngredient recipeIngredient = new RecipeIngredient(0, ingredientName, (float) amount, "unit"); // Birim eklemeyi unutmayın
                recipeIngredient.setTotalAvailableAmount((float) totalAvailableAmount); // Toplam mevcut miktarı ayarlayın
                ingredients.add(recipeIngredient);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ingredients;
    }

    // Malzemeleri al ve virgülle ayır
    private String getIngredientsForRecipe(int recipeId) {
        StringBuilder ingredients = new StringBuilder();
        String query = "SELECT MalzemeAdi, MalzemeMiktar FROM TarifMalzeme ri JOIN Malzemeler m ON ri.MalzemeID = m.MalzemeID WHERE ri.TarifID = ?";

        try (Connection conn = DbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, recipeId);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String ingredientName = rs.getString("MalzemeAdi");
                double amount = rs.getDouble("MalzemeMiktar");
                ingredients.append(ingredientName).append(" (").append(amount).append("), ");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Son iki karakteri (", ") kaldır
        if (ingredients.length() > 0) {
            ingredients.setLength(ingredients.length() - 2);
        }

        return ingredients.toString();
    }

    @FXML
    private void addIngredient() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/addIngredient.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Malzeme Ekle");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // Yeni malzemeleri yeniden yükle
            loadIngredients();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Malzeme ekleme penceresi açılırken bir hata oluştu.");
        }
    }


    private void loadIngredients() {
        ObservableList<Ingredient> ingredients = FXCollections.observableArrayList();
        String query = "SELECT * FROM Malzemeler";

        try (Connection conn = DbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                int id = rs.getInt("MalzemeID");
                String name = rs.getString("MalzemeAdi");
                String unit = rs.getString("MalzemeBirim");
                String total = rs.getString("ToplamMiktar");
                double uPrice = rs.getDouble("BirimFiyat");
                ingredients.add(new Ingredient(id, name, unit, total, uPrice));
            }
            ingredientComboBox.setItems(ingredients);
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Malzemeler yüklenirken hata oluştu.");
        }
    }

    private void addIngredientToRecipe(int recipeId, int ingredientId, double amount) {
        String query = "INSERT INTO TarifMalzeme (TarifID, MalzemeID, MalzemeMiktar) VALUES (?, ?, ?)";

        try (Connection conn = DbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, recipeId);
            pstmt.setInt(2, ingredientId);
            pstmt.setDouble(3, amount);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Malzeme tarife eklenirken bir hata oluştu.");
        }
    }


    @FXML
    private void addIngredientToRecipe() {
        Ingredient selectedIngredient = ingredientComboBox.getValue();
        if (selectedIngredient == null) {
            System.out.println("Lütfen bir malzeme seçin.");
            return;
        }

        float amount;
        try {
            amount = Float.parseFloat(ingredientAmountField.getText());
        } catch (NumberFormatException e) {
            System.out.println("Geçerli bir miktar girin.");
            return;
        }

        // Malzemenin tekrar eklenmemesi
        if (recipeIngredients.stream().anyMatch(ri -> ri.getIngredientId() == selectedIngredient.getIngredientId())) {
            System.out.println("Bu malzeme zaten eklendi.");
            return;
        }

        RecipeIngredient recipeIngredient = new RecipeIngredient(
                selectedIngredient.getIngredientId(),
                selectedIngredient.getIngredientName(),
                amount,
                selectedIngredient.getUnit()
        );

        recipeIngredients.add(recipeIngredient);
        ingredientAmountField.clear();
        ingredientComboBox.getSelectionModel().clearSelection();
    }

    @FXML
    private void searchRecipe() {
        String searchTerm = searchField.getText().toLowerCase();
        recipeTable.getItems().clear(); // Tabloyu temizlemeden önce

        String query = "SELECT DISTINCT t.*, COALESCE(SUM(tm.MalzemeMiktar * m.BirimFiyat), 0) AS ToplamMaliyet " +
                "FROM Tarifler t " +
                "JOIN TarifMalzeme tm ON t.TarifID = tm.TarifID " +
                "JOIN Malzemeler m ON tm.MalzemeID = m.MalzemeID " +
                "WHERE LOWER(t.TarifAdi) LIKE ? OR " +
                "LOWER(m.MalzemeAdi) LIKE ? " +
                "GROUP BY t.TarifID";

        try (Connection conn = DbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            String searchPattern = "%" + searchTerm + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);

            try (ResultSet rs = stmt.executeQuery()) {
                recipeList.clear(); // Her aramada önce listeyi temizle
                while (rs.next()) {
                    int id = rs.getInt("TarifID");
                    String name = rs.getString("TarifAdi");
                    String category = rs.getString("Kategori");
                    int preparationTime = rs.getInt("HazirlamaSuresi");
                    String instructions = rs.getString("Talimatlar");
                    double totalCost = rs.getDouble("ToplamMaliyet");

                    // Burada malzemeleri almak için metodu çağır
                    String ingredientList = getIngredientsForRecipe(id);
                    List<RecipeIngredient> ingredients = getRecipeIngredients(id);

                    // Yeni tarif nesnesi oluştur ve listeye ekle
                    Recipe recipe = new Recipe(id, name, category, preparationTime, instructions, ingredientList);
                    recipe.setTotalCost(totalCost);
                    recipe.setRecipeIngredients(ingredients); // Malzemeleri ayarla
                    recipeList.add(recipe);
                }
                recipeTable.setItems(recipeList); // Filtrelenmiş tarifleri tabloya ekle
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Arama yapılırken bir hata oluştu.");
        }
    }



    @FXML
    private void refreshPage() {
        searchField.setText("");
        loadRecipes();
    }

    @FXML
    private void saveRecipe() {
        String name = recipeNameField.getText();
        String category = categoryField.getText();
        int preparationTime = Integer.parseInt(preparationTimeField.getText());
        String instructions = instructionsArea.getText();

        Recipe newRecipe = new Recipe(0, name, category, preparationTime, instructions, "");

        try (Connection conn = DbConnection.getConnection()) {
            // Tarif kaydet
            String sql = "INSERT INTO Tarifler (TarifAdi, Kategori, HazirlamaSuresi, Talimatlar) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, name);
            stmt.setString(2, category);
            stmt.setInt(3, preparationTime);
            stmt.setString(4, instructions);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Tarif kaydedilemedi, satır eklenemedi.");
            }

            // Yeni tarifin ID'sini al
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    newRecipe.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Tarif kaydedildi ancak ID alınamadı.");
                }
            }

            // Tarif malzemeleri ilişki tablosu kayıt
            for (RecipeIngredient recipeIngredient : recipeIngredients) {
                addIngredientToRecipe(newRecipe.getId(), recipeIngredient.getIngredientId(), recipeIngredient.getAmount());
            }

            loadRecipes();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleTableClick(MouseEvent event) {
        if (event.getClickCount() == 2) {
            openRecipeDetailPopup();
        }
    }

    @FXML
    private void openRecipeDetailPopup() {
        Recipe selectedRecipe = recipeTable.getSelectionModel().getSelectedItem();

        if (selectedRecipe == null) {
            System.out.println("Lütfen bir tarif seçin.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/recipeDetailPopup.fxml"));
            Parent root = loader.load();

            RecipeDetailController controller = loader.getController();
            controller.setRecipe(selectedRecipe);

            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.setTitle("Tarif Detayları");
            popupStage.setScene(new Scene(root));
            popupStage.showAndWait();
            recipeTable.refresh();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Popup açılırken hata oluştu.");
        }
    }
}
