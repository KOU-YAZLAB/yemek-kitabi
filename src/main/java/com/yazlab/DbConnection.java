package com.yazlab;

import java.sql.*;

public class DbConnection {
    final static String URL = "jdbc:mysql://localhost:3306/yazlab_db";
    final static String NAME = "root";
    final static String PASS = "158358";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, NAME, PASS);
    }

    public static void updateRecipe(Recipe recipe) {
        String query = "UPDATE Tarifler SET TarifAdi=?, Kategori=?, HazirlamaSuresi=?, Talimatlar=? WHERE TarifID=?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, recipe.getName());
            stmt.setString(2, recipe.getCategory());
            stmt.setInt(3, recipe.getPreparationTime());
            stmt.setString(4, recipe.getInstructions());
            stmt.setInt(5, recipe.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Tarif güncellenirken hata oluştu.");
        }
    }

    public DbConnection Connect() {
        try {
            Connection connection = DriverManager.getConnection(URL, NAME, PASS);
            System.out.println("Bağlantı Başarılı. -> " + URL + ":" + NAME);

            String createDatabaseSQL = "CREATE DATABASE IF NOT EXISTS `" + "yazlab_db" + "`;";
            Statement statement = connection.createStatement();
            statement.executeUpdate(createDatabaseSQL);
            System.out.println("Veritabanı oluşturuldu veya zaten mevcut.");

            // Tarifler Tablosu
            String createTariflerTableSQL = "CREATE TABLE IF NOT EXISTS Tarifler (" +
                    "TarifID INT AUTO_INCREMENT PRIMARY KEY, " +
                    "TarifAdi VARCHAR(255) NOT NULL, " +
                    "Kategori VARCHAR(100) NOT NULL, " +
                    "HazirlamaSuresi INT NOT NULL, " +
                    "Talimatlar TEXT NOT NULL" +
                    ");";
            statement.executeUpdate(createTariflerTableSQL);
            System.out.println("Tarifler tablosu oluşturuldu.");

            // Malzemeler tablosu
            String createMalzemelerTableSQL = "CREATE TABLE IF NOT EXISTS Malzemeler (" +
                    "MalzemeID INT AUTO_INCREMENT PRIMARY KEY, " +
                    "MalzemeAdi VARCHAR(255) NOT NULL, " +
                    "ToplamMiktar VARCHAR(100) NOT NULL, " +
                    "MalzemeBirim VARCHAR(50) NOT NULL, " +
                    "BirimFiyat DECIMAL(10, 2) NOT NULL" +
                    ");";
            statement.executeUpdate(createMalzemelerTableSQL);
            System.out.println("Malzemeler tablosu oluşturuldu.");

            // Tarif-Malzeme ilişkisi tablosu
            String createTarifMalzemeTableSQL = "CREATE TABLE IF NOT EXISTS TarifMalzeme (" +
                    "TarifID INT, " +
                    "MalzemeID INT, " +
                    "MalzemeMiktar FLOAT NOT NULL, " +
                    "PRIMARY KEY (TarifID, MalzemeID), " +
                    "FOREIGN KEY (TarifID) REFERENCES Tarifler(TarifID) ON DELETE CASCADE, " +
                    "FOREIGN KEY (MalzemeID) REFERENCES Malzemeler(MalzemeID) ON DELETE CASCADE" +
                    ");";
            statement.executeUpdate(createTarifMalzemeTableSQL);
            System.out.println("Tarif-Malzeme ilişkisi tablosu oluşturuldu.");

            statement.close();
            connection.close();
        } catch (SQLException e) {
            System.out.println("Bağlantı hatası: " + e.getMessage());
        }
        return null;
    }
}