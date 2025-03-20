package main.java.zenit.ui.tree;

import javafx.application.Platform;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FileTreeUpdater {
    private TreeView<String> treeView;

    public FileTreeUpdater(TreeView<String> treeView) {
        this.treeView = treeView;
    }

    public void addLibrariesToFileTree(List<File> libraries, File selectedFile) {
        if (!libraries.isEmpty()) {
            FileTreeItem<String> libNode = findTreeItem(new File(selectedFile, "lib"));

            if (libNode != null) {
                for (File jarFile : libraries) {
                    File newJar = new File(libNode.getFile(), jarFile.getName());
                    FileTreeItem<String> jarNode = new FileTreeItem<>(newJar, newJar.getName(), FileTreeItem.FILE, false);
                    libNode.getChildren().add(jarNode);
                }
                sortChildren(libNode);
            }
        }
    }

    public void removeLibrariesFromFileTree(List<File> libraries, File selectedFile) {
        if (!libraries.isEmpty()) {
            FileTreeItem<String> libNode = findTreeItem(new File(selectedFile, "lib"));

            if (libNode != null) {
                List<TreeItem<String>> itemsToRemove = markItemsForRemoval(libraries, selectedFile, libNode);
                Platform.runLater(() -> {
                    libNode.getChildren().removeAll(itemsToRemove);
                    treeView.refresh();
                });
            }
        }
    }

    private List<TreeItem<String>> markItemsForRemoval(List<File> libraries, File selectedFile, FileTreeItem<String> libNode) {
        List<TreeItem<String>> itemsToRemove = new ArrayList<>();

        for (File jarFile : libraries) {
            File target = new File(selectedFile, jarFile.getPath());
            for (TreeItem<String> child : libNode.getChildren()) {
                FileTreeItem<String> fileTreeItem = (FileTreeItem<String>) child;
                if (fileTreeItem.getFile().getAbsolutePath().equals(target.getAbsolutePath())) {
                    itemsToRemove.add(fileTreeItem);
                    break;
                }
            }
        }
        return itemsToRemove;
    }

    public FileTreeItem<String> findTreeItem(File file) {
        return findTreeItem((FileTreeItem<String>) treeView.getRoot(), file);
    }

    private FileTreeItem<String> findTreeItem(FileTreeItem<String> root, File file) {
        if (root.getFile().equals(file)) {
            return root;
        }
        for (TreeItem<String> child : root.getChildren()) {
            FileTreeItem<String> result = findTreeItem((FileTreeItem<String>) child, file);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    public void sortChildren(FileTreeItem<String> parent) {
        parent.getChildren().sort((o1, o2) -> {
            FileTreeItem<String> f1 = (FileTreeItem<String>) o1;
            FileTreeItem<String> f2 = (FileTreeItem<String>) o2;
            if (f1.getType() == FileTreeItem.PACKAGE && f2.getType() != FileTreeItem.PACKAGE) {
                return -1;
            } else if (f1.getType() != FileTreeItem.PACKAGE && f2.getType() == FileTreeItem.PACKAGE) {
                return 1;
            } else {
                return f1.getValue().compareTo(f2.getValue());
            }
        });
    }
}
