package com.gide.gide;

import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

import java.io.File;
import java.nio.file.Path;

public class MainWindowController {
    @FXML
    private TreeView<String> projectTree;

    public void loadProject(Path projectPath) {
        TreeItem<String> rootItem = new TreeItem<>(projectPath.getFileName().toString());
        createTree(projectPath.toFile(), rootItem);
        projectTree.setRoot(rootItem);
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