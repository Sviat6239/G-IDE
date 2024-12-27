package com.gide.gide;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class NewProjectController {
    @FXML
    private TextField projectNameField;
    @FXML
    private TextField projectLocationField;

    @FXML
    public void initialize() {
        String defaultLocation = System.getProperty("user.home") + "\\G-IDEProjects";
        projectLocationField.setText(defaultLocation);
    }

    @FXML
    protected void browseLocation() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Project Location");
        File selectedDirectory = directoryChooser.showDialog(new Stage());
        if (selectedDirectory != null) {
            projectLocationField.setText(selectedDirectory.getAbsolutePath());
        }
    }

    @FXML
    protected void createProject() {
        String projectName = projectNameField.getText();
        String projectLocation = projectLocationField.getText();

        if (projectName != null && !projectName.isEmpty() && projectLocation != null && !projectLocation.isEmpty()) {
            Path projectPath = Path.of(projectLocation, projectName);
            try {
                Files.createDirectories(projectPath.resolve("src"));
                Stage stage = (Stage) projectNameField.getScene().getWindow();
                stage.close();
                openMainWindow(projectPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void openMainWindow(Path projectPath) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/gide/gide/main-window.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 800, 600);
            MainWindowController controller = fxmlLoader.getController();
            controller.loadProject(projectPath);
            Stage stage = new Stage();
            stage.setTitle("G-IDE - " + projectPath.getFileName().toString());
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}