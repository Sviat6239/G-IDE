package com.gide.gide;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
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

import java.awt.Desktop;
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

        searchField.textProperty().addListener((observable, oldValue, newValue) -> filterProjects(newValue));

    }

    private void filterProjects(String query) {
        projectList.getItems().clear();
        File projectDir = Paths.get(System.getProperty("user.home"), "G-IDEProjects").toFile();
        if (projectDir.exists() && projectDir.isDirectory()) {
            for (File file : projectDir.listFiles()) {
                if (file.isDirectory() && file.getName().toLowerCase().contains(query.toLowerCase())) {
                    projectList.getItems().add(file.getName());
                }
            }
        }
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

                Stage currentStage = (Stage) projectList.getScene().getWindow();
                currentStage.close();
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
            TextInputDialog dialog = new TextInputDialog(selectedProject);
            dialog.setTitle("Rename Project");
            dialog.setHeaderText("Rename Project");
            dialog.setContentText("New name:");
            dialog.showAndWait().ifPresent(newName -> {
                Path projectPath = Paths.get(projectLocationField.getText(), selectedProject);
                Path newProjectPath = projectPath.resolveSibling(newName);
                try {
                    Files.move(projectPath, newProjectPath);
                    loadProjects();
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

                MenuItem openInExplorerItem = new MenuItem("Open in Explorer");
                openInExplorerItem.setOnAction(e -> openInExplorer());

                contextMenu.getItems().addAll(renameItem, deleteItem, openInExplorerItem);
                contextMenu.show(projectList, event.getScreenX(), event.getScreenY());
            }
        } else if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
            openProject();
        }
    }

    @FXML
    private void openInExplorer() {
        String selectedProject = projectList.getSelectionModel().getSelectedItem();
        if (selectedProject != null) {
            Path projectPath = Paths.get(projectLocationField.getText(), selectedProject);
            if (Files.exists(projectPath) && Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().open(projectPath.toFile());
                } catch (IOException e) {
                    showErrorDialog("Error", "Could not open project in Explorer.", e);
                }
            } else {
                showErrorDialog("Error", "Project directory does not exist or Desktop is not supported.", null);
            }
        } else {
            showErrorDialog("Error", "No project selected.", null);
        }
    }

    public void saveProjects() {
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
                sortProjects();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sortProjects() {
        ObservableList<String> sortedList = FXCollections.observableArrayList(projectList.getItems());
        FXCollections.sort(sortedList);
        projectList.setItems(sortedList);
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
            sortProjects();
        }
    }

    @FXML
    protected void openProjectFolder() {
        String selectedProject = projectList.getSelectionModel().getSelectedItem();
        if (selectedProject != null) {
            Path projectPath = Paths.get(projectLocationField.getText(), selectedProject);
            try {
                java.awt.Desktop.getDesktop().open(projectPath.toFile());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void openInExplorer(ActionEvent event) {
        String selectedProject = projectList.getSelectionModel().getSelectedItem();
        if (selectedProject != null) {
            File projectDir = new File(selectedProject);
            if (projectDir.exists() && Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().open(projectDir);
                } catch (IOException e) {
                    showErrorDialog("Error", "Could not open project in Explorer.", e);
                }
            } else {
                showErrorDialog("Error", "Project directory does not exist or Desktop is not supported.", null);
            }
        } else {
            showErrorDialog("Error", "No project selected.", null);
        }
    }

    private void showErrorDialog(String title, String header, Exception e) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        if (e != null) {
            alert.setContentText(e.getMessage());
        }
        alert.showAndWait();
    }
}