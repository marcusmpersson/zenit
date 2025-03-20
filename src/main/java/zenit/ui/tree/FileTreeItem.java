package main.java.zenit.ui.tree;

import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Extension of the TreeItem class with the ability to save a corresponding File-object
 * in the instance.
 * @author Alexander Libot, Louis Brown
 *
 * @param <T>
 */
public class FileTreeItem<T> extends TreeItem<T> {
	private File file;
	private int type;
	
	public static final int WORKSPACE = 100;
	public static final int PROJECT = 101;
	public static final int PACKAGE = 102;
	public static final int CLASS = 103;
	public static final int SRC = 104;
	public static final int FOLDER = 105;
	public static final int FILE = 106;
	public static final int INCOMPATIBLE = 107;


    private ImageView icon;
	private ImageView playIcon;
    
	
	/**
	 * @param file Corresponding file
	 * @param name
	 */
	public FileTreeItem(File file, T name, int type, boolean test) {
		super(name);
		this.file = file;
		this.type = type;
		
		setIcon(test);
	}
	
	public void setIcon(boolean test) {
		String url = null;
		switch(type) {
		case PROJECT: url = "/zenit/ui/tree/project.png"; break;
		case PACKAGE: url = "/zenit/ui/tree/package.png"; break;
		case CLASS: url = "/zenit/ui/tree/class.png"; break;
		case SRC: url = "/zenit/ui/tree/src.png"; break;
		case FOLDER: url = "/zenit/ui/tree/folder.png"; break;
		case FILE: url = "/zenit/ui/tree/file.png"; break;
		case INCOMPATIBLE: url = "/zenit/ui/tree/incompatible.png"; break;
		}

		if (!test) {
			if (url != null) {
				icon = new ImageView(new Image(getClass().getResource(url).toExternalForm()));
				icon.setFitHeight(16);
				icon.setFitWidth(16);
				icon.setSmooth(true);
				this.setGraphic(icon);
			}


			if (isRunnableJavaClass(file)) {
				addPlayIcon();
			}
		}
	}

	/**
	 * Add a play icon to the right of the class icon
	 * @author Louis Brown
	 */
	public void addPlayIcon() {
		if (playIcon == null) {
			playIcon = new ImageView(new Image(getClass().getResource("/zenit/ui/tree/play.png").toExternalForm()));
			playIcon.setFitHeight(16);
			playIcon.setFitWidth(16);
			playIcon.setSmooth(true);

			// Add play icon to the right of the class icon
			this.setGraphic(new javafx.scene.layout.HBox(5, icon, playIcon));
		}
	}

	public boolean methodHasPlayIcon(File file){
		return playIcon != null;
	}

	/**
	 * Remove the play icon from the right of the class icon
	 * @author Louis Brown
	 */
	public void removePlayIcon() {
		if (playIcon != null) {
			this.setGraphic(icon);
			playIcon = null;
		}
	}

	/**
	 * Set the corresponding file
	 */
	public void setFile(File file) {
		this.file = file;
	}
	
	/**
	 * Get the corresponding file
	 */
	public File getFile() {
		return file;
	}
	
	public int getType() {
		return type;
	}
	
	public String getStringType() {
		String stringType;
		switch (type) {
		case PROJECT: stringType = "project"; break;
		case PACKAGE: stringType = "package"; break;
		case CLASS: stringType = "class"; break;
		case SRC: stringType = "src-folder"; break;
		case FOLDER: stringType = "folder"; break;
		case FILE: stringType = "file"; break;
		case INCOMPATIBLE: stringType = "incompatible"; break;
		default: stringType = null;
		}
		
		return stringType;
	}

	/**
	 * Check if the given file is a Java class with a main method
	 * @param file
	 * @return boolean true if the file is a Java class with a main
	 * @author Louis Brown
	 */
	public boolean isRunnableJavaClass(File file) {
		if (file.getName().endsWith(".java")) {
			try {
				// Check if the file contains a main method
				String content = new String(Files.readAllBytes(file.toPath()));
				return content.contains("public static void main(String[] args)");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	public void addIconIfMethodRunnable(File file){
		if (isRunnableJavaClass(file)) {
			addPlayIcon();
		}
	}

	/**
	 * Get the play icon
	 * @return ImageView playIcon
	 * @author Louis Brown
	 */
	public ImageView getPlayIcon() {
		return playIcon;
	}
}