package com.gide.gide;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class CodeEditorController {
    @FXML
    private TreeView<String> projectTree;
    @FXML
    private TextArea textArea;

    @FXML
    protected void newProject() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("new-project.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 600, 200);
            Stage stage = new Stage();
            stage.setTitle("New Project");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(scene);
            stage.showAndWait();
            Stage mainStage = (Stage) textArea.getScene().getWindow();
            mainStage.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void openFile() {
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(new Stage());
        if (file != null) {
            try {
                String content = Files.readString(Path.of(file.getPath()));
                textArea.setText(content);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    protected void saveFile() {
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showSaveDialog(new Stage());
        if (file != null) {
            try {
                Files.writeString(Path.of(file.getPath()), textArea.getText());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    protected void closeProject() {
        switchToProjectSelection();
    }

    @FXML
    protected void exit() {
        System.exit(0);
    }

    private void switchToProjectSelection() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("project-selection.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 800, 600);
            Stage stage = (Stage) textArea.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void initialize() {
        // Initialize project tree if needed
    }

    private void createTree(File file, TreeItem<String> parentItem) {
        File[] files = file.listFiles();
        if (files != null) {
            for (File f : files) {
                TreeItem<String> item = new TreeItem<>(f.getName());
                parentItem.getChildren().add(item);
                if (f.isDirectory()) {
                    createTree(f, item);
                }
            }
        }
    }
}