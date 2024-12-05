package com.yazlab;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class TarifUygulamasi extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            primaryStage.setTitle("Tarif Uygulaması");
            primaryStage.setScene(scene);
            primaryStage.setWidth(800);
            primaryStage.setHeight(600);
            primaryStage.show();

            DbConnection db = new DbConnection();
            db.Connect();
        } catch (Exception e) {
            System.out.println(getClass().getResource("/main.fxml"));

            e.printStackTrace();
            System.out.println("FXML dosyası yüklenirken hata oluştu. Lütfen dosya yolunu kontrol edin.");
        }
    }
}