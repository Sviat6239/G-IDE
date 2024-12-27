package com.gide.gide;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class ProjectSelectionController {
    @FXML
    private TextField searchField;
    @FXML
    private ListView<String> projectList;
    @FXML
    private TextField projectLocationField;

    private static final String PROJECTS_FILE = "projects.txt";

    @FXML
    public void initialize() {
        loadProjects();
        projectList.setOnMouseClicked(this::handleMouseClick);

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
    protected void newProject() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("new-project.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 600, 200);
            Stage stage = new Stage();
            stage.setTitle("New Project");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(scene);
            stage.showAndWait();
            refreshProjectList();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void openProject() {
        String selectedProject = projectList.getSelectionModel().getSelectedItem();
        if (selectedProject != null) {
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("code-editor.fxml"));
                Scene scene = new Scene(fxmlLoader.load(), 800, 600);
                Stage stage = new Stage();
                stage.setTitle("Project - " + selectedProject);
                stage.setScene(scene);
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    protected void cloneRepository() {
        // Implement clone repository functionality
    }

    @FXML
    protected void customizeIDE() {
        // Implement customize IDE functionality
    }

    @FXML
    protected void deleteProject() {
        String selectedProject = projectList.getSelectionModel().getSelectedItem();
        if (selectedProject != null) {
            projectList.getItems().remove(selectedProject);
            saveProjects();
        }
    }

    @FXML
    protected void renameProject() {
        String selectedProject = projectList.getSelectionModel().getSelectedItem();
        if (selectedProject != null) {
            TextInputDialog dialog = new TextInputDialog(Paths.get(selectedProject).getFileName().toString());
            dialog.setTitle("Rename Project");
            dialog.setHeaderText("Rename Project");
            dialog.setContentText("New name:");

            Optional<String> result = dialog.showAndWait();
            result.ifPresent(newName -> {
                Path source = Paths.get(selectedProject);
                try {
                    Path target = source.resolveSibling(newName);
                    Files.move(source, target);
                    projectList.getItems().set(projectList.getItems().indexOf(selectedProject), target.toString());
                    saveProjects();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    private void handleMouseClick(MouseEvent event) {
        if (event.getButton() == MouseButton.SECONDARY) {
            String selectedProject = projectList.getSelectionModel().getSelectedItem();
            if (selectedProject != null) {
                ContextMenu contextMenu = new ContextMenu();

                MenuItem renameItem = new MenuItem("Rename Project");
                renameItem.setOnAction(e -> renameProject());

                MenuItem deleteItem = new MenuItem("Delete Project");
                deleteItem.setOnAction(e -> deleteProject());

                contextMenu.getItems().addAll(renameItem, deleteItem);
                contextMenu.show(projectList, event.getScreenX(), event.getScreenY());
            }
        } else if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
            openProject();
        }
    }

    private void saveProjects() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(PROJECTS_FILE))) {
            for (String project : projectList.getItems()) {
                writer.write(project);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadProjects() {
        File file = new File(PROJECTS_FILE);
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    projectList.getItems().add(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void refreshProjectList() {
        projectList.getItems().clear();
        File projectDir = Paths.get(System.getProperty("user.home"), "G-IDEProjects").toFile();
        if (projectDir.exists() && projectDir.isDirectory()) {
            for (File file : projectDir.listFiles()) {
                if (file.isDirectory()) {
                    projectList.getItems().add(file.getName());
                }
            }
        }
    }
}