package com.gide.gide;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import java.io.ObjectOutputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.FileInputStream;
import java.io.Serializable;

import java.io.File;
import java.io.IOException;
import java.util.function.Predicate;

public class ProjectTreeController {
    @FXML
    private TreeView<String> projectTree;
    @FXML
    private TextField searchField;

    private static final String TREE_STATE_FILE = "tree_state.ser";

    @FXML
    public void initialize() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Open Project Directory");
        File selectedDirectory = directoryChooser.showDialog(new Stage());
        if (selectedDirectory != null) {
            TreeItem<String> rootItem = new TreeItem<>(selectedDirectory.getName());
            createTree(selectedDirectory, rootItem);
            projectTree.setRoot(rootItem);
        }

        projectTree.setCellFactory(tv -> {
            TreeCell<String> cell = new TreeCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? null : item);
                }
            };

            cell.setOnMouseClicked(event -> {
                if (!cell.isEmpty() && event.getButton() == MouseButton.SECONDARY) {
                    ContextMenu contextMenu = new ContextMenu();

                    MenuItem createFile = new MenuItem("Create File");
                    createFile.setOnAction(e -> createFile(cell.getTreeItem()));

                    MenuItem createFolder = new MenuItem("Create Folder");
                    createFolder.setOnAction(e -> createFolder(cell.getTreeItem()));

                    MenuItem rename = new MenuItem("Rename");
                    rename.setOnAction(e -> renameItem(cell.getTreeItem()));

                    MenuItem delete = new MenuItem("Delete");
                    delete.setOnAction(e -> deleteItem(cell.getTreeItem()));

                    MenuItem openInExplorer = new MenuItem("Open in Explorer");
                    openInExplorer.setOnAction(e -> openInExplorer(cell.getTreeItem()));

                    MenuItem expandAll = new MenuItem("Expand All");
                    expandAll.setOnAction(e -> expandTreeItem(cell.getTreeItem()));

                    MenuItem collapseAll = new MenuItem("Collapse All");
                    collapseAll.setOnAction(e -> collapseTreeItem(cell.getTreeItem()));

                    contextMenu.getItems().addAll(createFile, createFolder, rename, delete, openInExplorer, expandAll, collapseAll);
                    cell.setContextMenu(contextMenu);
                }
            });

            cell.setOnDragDetected(event -> {
                if (!cell.isEmpty()) {
                    Dragboard db = cell.startDragAndDrop(TransferMode.MOVE);
                    ClipboardContent content = new ClipboardContent();
                    content.putString(cell.getItem());
                    db.setContent(content);
                    event.consume();
                }
            });

            cell.setOnDragOver(event -> {
                if (event.getGestureSource() != cell && event.getDragboard().hasString()) {
                    event.acceptTransferModes(TransferMode.MOVE);
                }
                event.consume();
            });

            cell.setOnDragDropped(event -> {
                Dragboard db = event.getDragboard();
                boolean success = false;
                if (db.hasString()) {
                    TreeItem<String> draggedItem = findTreeItem(projectTree.getRoot(), db.getString());
                    TreeItem<String> newParent = cell.getTreeItem();
                    if (draggedItem != null && newParent != null && !draggedItem.equals(newParent)) {
                        draggedItem.getParent().getChildren().remove(draggedItem);
                        newParent.getChildren().add(draggedItem);
                        success = true;
                    }
                }
                event.setDropCompleted(success);
                event.consume();
            });

            return cell;
        });

        searchField.textProperty().addListener((observable, oldValue, newValue) -> filterTree(newValue));

        loadProjectTree();
    }

    @FXML
    private void refreshTree() {
        loadProjectTree();
    }

    private void loadProjectTree() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Open Project Directory");
        File selectedDirectory = directoryChooser.showDialog(new Stage());
        if (selectedDirectory != null) {
            TreeItem<String> rootItem = new TreeItem<>(selectedDirectory.getName());
            createTree(selectedDirectory, rootItem);
            projectTree.setRoot(rootItem);
        }
    }

    @FXML
    private void saveTreeState() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(TREE_STATE_FILE))) {
            oos.writeObject(projectTree.getRoot());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void loadTreeState() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(TREE_STATE_FILE))) {
            TreeItem<String> root = (TreeItem<String>) ois.readObject();
            projectTree.setRoot(root);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private TreeItem<String> findTreeItem(TreeItem<String> root, String value) {
        if (root.getValue().equals(value)) {
            return root;
        }
        for (TreeItem<String> child : root.getChildren()) {
            TreeItem<String> result = findTreeItem(child, value);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    private void createFile(TreeItem<String> parentItem) {
        // Implement file creation logic
    }

    private void createFolder(TreeItem<String> parentItem) {
        // Implement folder creation logic
    }

    private void renameItem(TreeItem<String> item) {
        // Implement rename logic
    }

    private void deleteItem(TreeItem<String> item) {
        // Implement delete logic
    }

    private void openInExplorer(TreeItem<String> item) {
        try {
            File file = new File(item.getValue());
            if (file.exists()) {
                new ProcessBuilder("explorer.exe", "/select,", file.getAbsolutePath()).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    private void filterTree(String searchText) {
        TreeItem<String> root = projectTree.getRoot();
        if (root != null) {
            root.getChildren().clear();
            createTree(new File(root.getValue()), root);
            if (!searchText.isEmpty()) {
                Predicate<TreeItem<String>> predicate = item -> item.getValue().toLowerCase().contains(searchText.toLowerCase());
                filterTreeItems(root, predicate);
            }
        }
    }

    private void filterTreeItems(TreeItem<String> parent, Predicate<TreeItem<String>> predicate) {
        parent.getChildren().removeIf(item -> !predicate.test(item) && item.getChildren().isEmpty());
        for (TreeItem<String> child : parent.getChildren()) {
            filterTreeItems(child, predicate);
        }
    }

    private void expandTreeItem(TreeItem<String> item) {
        item.setExpanded(true);
        for (TreeItem<String> child : item.getChildren()) {
            expandTreeItem(child);
        }
    }

    private void collapseTreeItem(TreeItem<String> item) {
        item.setExpanded(false);
        for (TreeItem<String> child : item.getChildren()) {
            collapseTreeItem(child);
        }
    }
}