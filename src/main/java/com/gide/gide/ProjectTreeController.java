package com.gide.gide;

import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;

public class ProjectTreeController {
    @FXML
    private TreeView<String> projectTree;

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