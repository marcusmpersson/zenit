package main.java.zenit.ui.tree;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.*;
import main.java.zenit.filesystem.ProjectFile;
import main.java.zenit.filesystem.helpers.CodeSnippets;
import main.java.zenit.filesystem.helpers.FileNameHelpers;
import main.java.zenit.ui.MainController;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Class that extends {@link javafx.scene.control.ContextMenu} with static menu items with dynamic
 * text. Also contains event handler.
 * @author Alexander Libot, Louis Brown
 *
 */
public class TreeContextMenu extends ContextMenu implements EventHandler<ActionEvent>{
	
	private MainController controller;
	private FileTreeUpdater fileTreeUpdater;
	private TreeView<String> treeView;
	
	private Menu createItem = new Menu("New...");
	private MenuItem createClass = new MenuItem("New class");
	private MenuItem createInterface = new MenuItem("New interface");
	private MenuItem createPackage = new MenuItem("New package");
	private MenuItem renameItem = new MenuItem("Rename");
	private MenuItem deleteItem = new MenuItem("Delete");
	private MenuItem importJar = new MenuItem("Import jar");
	private MenuItem properties = new MenuItem("Properties");
	private MenuItem moveItem = new MenuItem("Move");
	private MenuItem dropItem = new MenuItem("Drop");

	private FileTreeItem<String> itemToMove = null;

	/**
	 * Creates a new {@link TreeContextMenu} that can manipulate a specific {@link
	 * javafx.scene.control.TreeView TreeView} instance and call methods in a specific
	 * {@link main.java.zenit.ui.MainController MainController}
	 * @param controller The {@link main.java.zenit.ui.MainController MainController} instance where methods
	 * will be called
	 * @param treeView The {@link javafx.scene.control.TreeView TreeView} instance which will
	 * be manipulated
	 */
	public TreeContextMenu(MainController controller, FileTreeUpdater fileTreeUpdater, TreeView<String> treeView) {
		super();
		this.controller = controller;
		this.fileTreeUpdater = fileTreeUpdater;
		this.treeView = treeView;
		initContextMenu();
	}
	
	/**
	 * Updates the menu items with dynamic text.
	 * @param selectedNode The name of the node in the tree to be inserted dynamically
	 */
	private void setContext(String selectedNode) {
		String renameItemTitle = String.format("Rename \"%s\"", selectedNode);
		String deleteItemTitle = String.format("Delete \"%s\"", selectedNode);
		renameItem.setText(renameItemTitle);
		deleteItem.setText(deleteItemTitle);

		FileTreeItem<String> selectedItem = (FileTreeItem<String>) treeView.getSelectionModel().getSelectedItem();
		if (selectedItem.getFile().isDirectory()) {
			if (!getItems().contains(createItem)) {
				getItems().add(0, createItem);
			}
			if (!createItem.getItems().contains(createPackage)) {
				createItem.getItems().add(createPackage);
			}
			if (!createItem.getItems().contains(createClass)) {
				createItem.getItems().add(createClass);
			}
			if (!createItem.getItems().contains(createInterface)) {
				createItem.getItems().add(createInterface);
			}
		} else {
			getItems().remove(createItem);
		}

		if (selectedItem.getType() == FileTreeItem.PROJECT) {
			getItems().add(importJar);
			getItems().add(properties);
		} else {
			getItems().remove(importJar);
			getItems().remove(properties);
		}
	}
	
	/**
	 * Overrides {@link javafx.scene.control.ContextMenu#show(Node, double, double) show(...)} in
	 * {@link javafx.scene.control.ContextMenu ContextMenu}. Dynamically updates the menu
	 * items before showing the context menu.
	 */
	@Override
	public void show(Node node, double x, double y) {
		TreeItem<String> selectedItem = treeView.getSelectionModel().getSelectedItem();
		
		if (selectedItem != null) {
			setContext(selectedItem.getValue());
		}
		
		super.show(node, x, y);
	}
	
	/**
	 * Initializes the context menu by adding all menus and menu items and setting
	 * action listeners.
	 */
	private void initContextMenu() {
		createItem.getItems().add(createClass);
		createItem.getItems().add(createInterface);
		getItems().addAll(createItem, renameItem, deleteItem, moveItem, dropItem);
		createClass.setOnAction(this);
		createInterface.setOnAction(this);
		renameItem.setOnAction(this);
		deleteItem.setOnAction(this);
		createPackage.setOnAction(this);
		importJar.setOnAction(this);
		properties.setOnAction(this);
		moveItem.setOnAction(this);
		dropItem.setOnAction(this);
		dropItem.setVisible(false);
	}
	
	/**
	 * To create a new file, calls {@link main.java.zenit.ui.MainController#createFile(File, int)}
	 * @param typeCode The type of item to be created. Use constants from {@link 
	 * main.java.zenit.filesystem.helpers.CodeSnippets CodeSnippets}
	 */
	private void newFile(int typeCode) {
		FileTreeItem<String> parent = (FileTreeItem<String>) 
				treeView.getSelectionModel().getSelectedItem();
		File newFile = controller.createFile(parent.getFile(), typeCode);
		if (newFile != null) {
			FileTreeItem<String> newItem = new FileTreeItem<String>(newFile, newFile.getName(), FileTreeItem.CLASS, false);
			parent.getChildren().add(newItem);
			fileTreeUpdater.sortChildren(parent);

			controller.openFile(newFile);
		}
	}

	/**
	 * Moves a file to a new directory. Calls {@link main.java.zenit.ui.MainController#moveFile(File, File)}
	 * @param selectedItem
	 * @param destinationDir
	 * @author Louis Brown
	 */
	private void moveFile(FileTreeItem<String> selectedItem, File destinationDir) {
		File oldFile = selectedItem.getFile();
		File newFile = new File(destinationDir, oldFile.getName());

		if (newFile.exists()) {
			Alert alert = new Alert(Alert.AlertType.ERROR);
			alert.setTitle("Move File Error");
			alert.setHeaderText("File Move Failed");
			alert.setContentText("A file with the name \"" + oldFile.getName() + "\" already exists in the destination directory.");
			alert.showAndWait();
			return;
		}

		newFile = controller.moveFile(oldFile, destinationDir);
		if (newFile != null) {
			selectedItem.setFile(newFile);
			selectedItem.setValue(newFile.getName());
			FileTree.changeFileForNodes(selectedItem, newFile);

			TreeItem<String> parent = selectedItem.getParent();
			parent.getChildren().remove(selectedItem);

			FileTreeItem<String> newParent = fileTreeUpdater.findTreeItem(destinationDir);
			if (newParent != null) {
				newParent.getChildren().add(selectedItem);
			}

			String newPackageName = FileNameHelpers.getPackagenameFromFile(newFile);
			try {
				FileNameHelpers.updatePackageName(newFile, newPackageName);
			} catch (IOException e) {
				e.printStackTrace();
			}

			updateOpenTabs(oldFile, newFile);
		}
	}

	/**
	 * Updates the open tabs in the TabPane.
	 * @param oldFile
	 * @param newFile
	 * @author Louis Brown
	 */
	public void updateOpenTabs(File oldFile, File newFile) {
		TabPane tabPane = controller.getTabPane();
		Tab oldTab = findTabByFile(tabPane, oldFile);
		if (oldTab != null) {
			tabPane.getTabs().remove(oldTab);
			controller.openFile(newFile);
		}
	}

	/**
	 * Finds a tab by a file. Iterates through all tabs in the TabPane.
	 * @param tabPane
	 * @param file
	 * @return Tab if found, null if not found
	 * @author Louis Brown
	 */
	private Tab findTabByFile(TabPane tabPane, File file) {
		String filePath = file.getAbsolutePath();
		for (Tab tab : tabPane.getTabs()) {
			if (tab.getUserData() != null) {
				String tabFilePath = tab.getUserData().toString();
				if (tabFilePath.equals(filePath)) {
					return tab;
				}
			}
		}
		return null;
	}

	/**
	 * Event handler for TreeContextMenu. Calls different methods in {@link main.java.zenit.ui.MainController
	 * MainController} depending on input.
	 */
	@Override
	public void handle(ActionEvent actionEvent) {
		FileTreeItem<String> selectedItem = (FileTreeItem<String>) treeView.getSelectionModel().getSelectedItem();
		File selectedFile = selectedItem.getFile();
		
		if (actionEvent.getSource().equals(createClass)) {
			newFile(CodeSnippets.CLASS);
		} else if (actionEvent.getSource().equals(createInterface)) {
			newFile(CodeSnippets.INTERFACE);
		} else if (actionEvent.getSource().equals(renameItem)) {
			File newFile = controller.renameFile(selectedFile);
			if (newFile != null) {
				selectedItem.setFile(newFile);
				selectedItem.setValue(newFile.getName());
				FileTree.changeFileForNodes(selectedItem, selectedItem.getFile());
				updateOpenTabs(selectedFile, newFile);
			}
		} else if (actionEvent.getSource().equals(deleteItem)) {
			Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
			alert.setTitle("Delete File");
			alert.setHeaderText("Are you sure you want to delete this file?");
			alert.setContentText("File: " + selectedFile.getName());

			ButtonType result = alert.showAndWait().orElse(ButtonType.CANCEL);
			if (result == ButtonType.OK) {
				if (selectedFile.getName().toLowerCase().endsWith(".jar")) {
					ProjectFile projectFile = new ProjectFile(getProjectRootFile().getPath());
					List<String> libraryPaths = new ArrayList<>();
					libraryPaths.add(String.valueOf(selectedFile));
					controller.removeLibraries(libraryPaths, projectFile);
					selectedItem.getParent().getChildren().remove(selectedItem);
				} else {
					controller.deleteFile(selectedFile);
					selectedItem.getParent().getChildren().remove(selectedItem);
					controller.updateComboBox();
				}
			}
		} else if (actionEvent.getSource().equals(createPackage)) {
			File packageFile = controller.newPackage(selectedFile);
			if (packageFile != null) {
				FileTreeItem<String> packageNode = new FileTreeItem<String>(packageFile, packageFile.getName(), FileTreeItem.PACKAGE, false);
				selectedItem.getChildren().add(packageNode);
				fileTreeUpdater.sortChildren(selectedItem);
			}
		} else if (actionEvent.getSource().equals(importJar)) {
			ProjectFile projectFile = new ProjectFile(selectedFile.getPath());
			List<File> libraries = controller.chooseAndImportLibraries(projectFile);
			fileTreeUpdater.addLibrariesToFileTree(libraries, selectedFile);
		} else if (actionEvent.getSource().equals(properties) && selectedItem.getType() == FileTreeItem.PROJECT) {
			ProjectFile projectFile = new ProjectFile(selectedFile.getPath());
			controller.showProjectProperties(projectFile);
		} else if (actionEvent.getSource().equals(moveItem)) {
			itemToMove = selectedItem;
			dropItem.setVisible(true);
		} else if (actionEvent.getSource().equals(dropItem) && itemToMove != null) {
			moveFile(itemToMove, selectedItem.getFile());
			itemToMove = null;
			dropItem.setVisible(false);
		}
	}

	private File getProjectRootFile() {
		FileTreeItem<String> workspaceRoot = (FileTreeItem<String>) treeView.getRoot();
		if (workspaceRoot != null) {
			for (TreeItem<String> child : workspaceRoot.getChildren()) {
				FileTreeItem<String> projectItem = (FileTreeItem<String>) child;
				if (projectItem.getType() == FileTreeItem.PROJECT) {
					return projectItem.getFile();
				}
			}
		}
		return null;
	}
}
