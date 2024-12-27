package com.gide.gide;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

import java.net.URL;

public class Main extends Application {
    @Override
    public void start(Stage stage) {
        try {
            URL fxmlLocation = getClass().getResource("/com/gide/gide/project-selection.fxml");
            if (fxmlLocation == null) {
                throw new RuntimeException("FXML file not found");
            }
            System.out.println("FXML location: " + fxmlLocation);

            FXMLLoader fxmlLoader = new FXMLLoader(fxmlLocation);
            Scene scene = new Scene(fxmlLoader.load(), 800, 600);
            stage.setTitle("Project Selection");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            showErrorDialog("Error loading FXML", "An error occurred while loading the FXML file.", e);
        }
    }

    private void showErrorDialog(String title, String header, Exception e) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(e.getMessage());
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch();
    }
}