package main.java.zenit.filesystem;

import javafx.stage.Stage;
import main.java.zenit.filesystem.helpers.CodeSnippets;
import main.java.zenit.filesystem.metadata.Metadata;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

/**
 * Class for controlling and manipulating the file system of a project.
 * @author Alexander Libot
 *
 */
public class FileController {
	
	private static File workspace;
	private static final String  LIB_DIRECTORY = "lib";//Used as a base-file for all files

	
	/**
	 * Creates a new file controller to manipulate the file system
	 */
	public FileController(File workspace) {
		this.workspace = workspace;
	}
	
	//Getters
	/**
	 * Returns the current {@code workspace}-file
	 */
	public static File getWorkspace() {
		return workspace;
	}

	private File getOrCreateLibDirectory() {
		File libDir = new File(workspace, LIB_DIRECTORY);
		if (!libDir.exists()) {
			boolean created = libDir.mkdir();
			if (created) {
				System.out.println("Created lib directory: " + libDir.getAbsolutePath());
			} else {
				System.err.println("Failed to create lib directory.");
			}
			libDir.mkdir();
		}
		return libDir;
	}

	public boolean addLibrary(Stage stage, File jarFile) {
		if (jarFile == null || !jarFile.exists()) {
			System.err.println("FileController.addLibrary: The provided file does not exist.");
			return false;
		}
		try {
			File libDir = getOrCreateLibDirectory();
			File targetFile = new File(libDir, jarFile.getName());
			Files.copy(jarFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
			return updateClasspath(targetFile, true);
		} catch (IOException e) {
			System.err.println("FileController.addLibrary: Error adding library - " + e.getMessage());
			return false;
		}
	}

	public boolean chooseAndImportLibrary(Stage stage, List<File> jarFiles) {
		if (jarFiles == null || jarFiles.isEmpty()) {
			System.err.println("No JAR files selected.");
			return false;
		}
		boolean allSuccessful = true;
		for (File jarFile : jarFiles) {
			boolean success = addLibrary(stage, jarFile);
			if (!success) {
				allSuccessful = false;
				System.err.println("Failed to import library: " + jarFile.getName());
			}
		}
		return allSuccessful;
	}



	private boolean updateClasspath(File jarFile, boolean isAdding) {
		File classpathFile = new File(workspace, ".classpath");
		try {
			if (!classpathFile.exists()) {
				classpathFile.createNewFile();
			}
			List<String> lines = Files.readAllLines(classpathFile.toPath());
			String entry = "<classpathentry kind=\"lib\" path=\"" + LIB_DIRECTORY + "/" + jarFile.getName() + "\"/>";

			if (isAdding && !lines.contains(entry)) {
				lines.add(entry);
				System.out.println("Added to classpath: " + entry);
			} else if (!isAdding) {
				lines.remove(entry);
				System.out.println("Removed from classpath: " + entry);
			}
			Files.write(classpathFile.toPath(), lines);
			return true;
		} catch (IOException e) {
			System.err.println("FileController.updateClasspath: Error updating classpath - " + e.getMessage());
			return false;
		}
	}


	/**
	 * Creates a new .java file from the File-objects using 
	 * {@link JavaFileHandler#createFile(int, File, String) ClassHandler}
	 * @param file File to be created
	 * @param content Content of the file, may be null.
	 * @param typeCode If new file, the type of code snippet to be inserted.
	 * Use {@link zenit.filesystem.helpers.CodeSnippets#newSnippet(int, String, String)
	 * CodeSnippets} constants.
	 * @return Returns the created File-object if created, otherwise null
	 */
	public File createFile(File file, String content, int typeCode) {
		if (file != null) {
			try {
				return JavaFileHandler.createFile(file, content, typeCode);
			} catch (IOException ex) {
				System.out.println(ex.getMessage());
			}
		}
		return null;
	}
	
	/**
	 * Calls {@link #createFile(int, File, String) createFile} method with null 
	 * content parameter.
	 * @param file The file to be created
	 * @param typeCode If new file, the type of code snippet to be inserted.
	 * Use {@link zenit.filesystem.helpers.CodeSnippets#newSnippet(int, String, String)
	 * CodeSnippets} constants.
	 * @return See {@link #createFile(int, File, String) createFile} for return value
	 */
	public File createFile(File file, int typeCode) {
		return createFile(file, null, typeCode);
	}
	
	/**
	 * Calls {@link #createFile(int, File, String) createFile} method with {@code typeCode}
	 * for empty code snippet.
	 * @param file The file to be created
	 * @param content The content to write to file. Can be null.
	 * @return See {@link #createFile(int, File, String) createFile} for return value
	 */
	public File createFile(File file, String content) {
		return createFile(file, content, CodeSnippets.EMPTY);
	}
	
	/**
	 * Tries to read a java-file from disk. Prints an error message if file can't be read.
	 * @param file The file to read from disk.
	 * @return The content of read file if read, otherwise null.
	 */
	public String readFileContent(File file) {
		if (file != null) {
			try {
				return JavaFileHandler.readFile(file);
			} catch (IOException ex) {
				System.err.println("FileController.readFileContent: " + ex.getMessage());
			}
		}
		return null;
	}

	public static void test(){
		System.out.println("test");
	}
	
	/**
	 * Reads a file line by line.
	 * @param file The File to read.
	 * @return A String containing all the lines of the File content. 
	 * Null if the file could not be read.
	 */
	public static String readFile(File file) {
		if (file == null || file.isDirectory()) {
			return "";
		}

		try {
			FileReader fileReader = new FileReader(file);
			var bufferedReader = new BufferedReader(fileReader);

			StringBuilder builder = new StringBuilder();
			String line;

			while ((line = bufferedReader.readLine()) != null) {
				builder.append(line);
				builder.append(System.lineSeparator());
			}

			bufferedReader.close();
			fileReader.close();

			return builder.toString();
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();

			// TODO: give the user feedback that the file could not be found
		} catch (IOException ex) {
			ex.printStackTrace();

			// TODO: handle IO exception
		}

		return null;
	}
	
	/**
	 * Tries to write {@code content} to {@code file}. Prints error message if content
	 * can't be written.
	 * @param file The file to write over.
	 * @param dsd The content to write to disk
	 */
	public static boolean writeFile(File file, String content) {
		if (file != null && content != null) {
			try {
				JavaFileHandler.saveFile(file, content); //Tries to save file
				return true;
			} catch (IOException ex) {
				System.err.println("FileController.writeFile: " + ex.getMessage());
			}
		}
		return false;
	}
	
	/**
	 * Renames a file-object and it's file path using {@link main.java.zenit.filesystem.JavaFileHandler
	 * #renameFile(File, String) renameFile} method if {@code file} is a file or
	 * {@link main.java.zenit.filesystem.ProjectHandler#renameFolder(File, String) renameProject}
	 * method if {@code file} is a directory. Prints an error message if file couldn't
	 * be renamed.
	 * 
	 * @param file File to be renamed
	 * @param newName New name of file
	 * @return Renamed file if renamed, otherwise null
	 */
	public File renameFile(File file, String newName) {
		File newFile = null;
		if (file != null && newName != null) {
			try {
				if (file.isDirectory()) {
					newFile = FolderHandler.renameFolder(file, newName);
				} else {
					newFile = JavaFileHandler.renameFile(file, newName);
				}
			} catch (IOException ex) {
				System.err.println("FileController.renameFile: " + ex.getMessage());
			}
		}
		return newFile;
	}
	
	/**
	 * Deletes a file from disk. Using {@link main.java.zenit.filesystem.JavaFileHandler
	 * #deleteFile(File) deleteFile} method if {@code file} is a file or
	 * {@link main.java.zenit.filesystem.ProjectHandler#deleteFolder(File) deleteProject}
	 * method if {@code file} is a directory. Prints an error message if file or a children
	 * file couldn't be deleted.
	 * @param file File to be deleted.
	 * @return True if deleted properly or false if file or a children file couldn't
	 * be deleted.
	 */
	public boolean deleteFile(File file) {
		boolean success = false;
		if (file != null) {
			try {
				if (file.isDirectory()) {
					FolderHandler.deleteFolder(file);
				} else {
					JavaFileHandler.deleteFile(file);
				}
				success = true;
			} catch (IOException ex) {
				System.err.println("FileController.deleteFile: " + ex.getMessage());
			}
		}
		return success;
	}
	
	/**
	 * Tries to create a new package using {@link PackageHandler#createPackage(File)}.
	 * Prints error message if package can't be created.
	 * @param file The package file to be created.
	 * @return {@code true} if package was created, otherwise {@code false}
	 */
	public boolean createPackage(File file) {	
		try {
			PackageHandler.createPackage(file);
			return true;
		} catch (IOException ex) {
			System.err.println("FileController.createPackage: " + ex.getMessage());
			return false;
		}
	}
	
	//Project methods
	
	/**
	 * Tries to create a new project using {@link main.java.zenit.filesystem.ProjectHandler
	 * #createNewProject(File) createNewProject} method in the set workspace.
	 * Prints an error message if project or any of its files couldn't be created.
	 * @param projectname Name of the new project
	 * @return Returns File-object of the created project if created, otherwise null
	 */
	public File createProject(String projectname) {
		if (projectname != null) {
			try {
				File file = new File(workspace + "/" + projectname);
				ProjectHandler.createNewProject(file);
				return file;
			} catch (IOException ex) {
				System.err.println("FileController.createProject: IOException: " + 
						ex.getMessage());
			}
		}
		return null;
	}
	
	/**
	 * Tries to create or overwrite new {@code res/workspace/workspace.dat} file
	 * and set {@code workspace} instance variable to new {@code workspace} file.
	 * @param workspace The new workspace file
	 * @return True if changed, otherwise false.
	 */
	public boolean changeWorkspace(File workspace) {
		boolean success = WorkspaceHandler.createWorkspace(workspace);
		if (success) {
			this.workspace = workspace;
		}
		return success;
	}
	
	/**
	 * Tries to import a folder to workspace
	 * @param source Folder to import
	 * @return The copied File
	 */
	public File importProject(File source) throws IOException {
			File target = ProjectHandler.importProject(source, workspace);
			return target;
	}
	
	//Library methods
	
	/**
	 * Tries to copy the files in parameter to project's lib-folder (creates one if it doesn't 
	 * already exist). Adds the build paths to metadata-file. Uses 
	 * {@link ProjectHandler#addInternalLibraries(List, ProjectFile)}.
	 * @param internalLibraryFiles Files to copy and create build paths to. File path should
	 * exclude project file path. Correct path: {@code lib/library.jar}
	 * @param projectFile The project the files should be copied to, must have a metadata-file.
	 * @return {@code true} if imported successfully, otherwise {@code false}. Note that files can
	 * be copied and still return {@code false}
	 */
	public boolean addInternalLibraries(List<File> internalLibraryFiles, ProjectFile projectFile) {
		try {
			return ProjectHandler.addInternalLibraries(internalLibraryFiles, projectFile);
		} catch (IOException e) {
			return false;
		}
	}
	
	/**
	 * Tries to remove the files in parameter from project's lib-folder. Removes the build paths
	 * from metadata-file. Uses {@link ProjectHandler#removeInternalLibraries(List, ProjectFile)}.
	 * @param internalLibraryPaths Files to remove, file path should exclude project file path.
	 * Correct path: {@code lib/library.jar}
	 * @param projectFile The project the files should be removed from, must have a metadata-file.
	 * @return {@code true} if removed successfully, otherwise {@code false}. Note that files can
	 * be removed and still return {@code false}
	 */
	public boolean removeInternalLibraries(List<String> internalLibraryPaths, ProjectFile projectFile) {
		return ProjectHandler.removeInternalLibraries(internalLibraryPaths, projectFile);
	}
	
	/**
	 * Tries to add build paths for files in parameter to project's metadata file.
	 * Uses {@link ProjectHandler#addExternalLibraries(List, ProjectFile)}.
	 * @param externalLibraryFiles Files to add, the file paths should contain the full file path
	 * from root.
	 * @param projectFile The project to which build paths should be made to. Project must have a
	 * metadata-file.
	 * @return {@code true} if build paths added successfully, otherwise {@code false}.
	 */
	public boolean addExternalLibraries(List<File> externalLibraryFiles, ProjectFile projectFile) {
		return ProjectHandler.addExternalLibraries(externalLibraryFiles, projectFile);
	}
	
	/**
	 * Tries to remove build paths for files in parameter from project's metadata file. Uses
	 * {@link ProjectHandler#removeExternalLibraries(List, ProjectFile)}.
	 * @param externalLibraryPaths Files to remove, the file path should contain the full file path
	 * from root.
	 * @param projectFile The project to which build paths should be removed from. Project must
	 * have a metadata-file.
	 * @return {@code true} if build paths removed successfully, otherwise {@code false}.
	 */
	public boolean removeExternalLibraries(List<String> externalLibraryPaths, 
			ProjectFile projectFile) {
		return ProjectHandler.removeExternalLibraries(externalLibraryPaths, projectFile);
	}
	
	//Metadata
	
	/**
	 * Updates metadata file to the latest version
	 * @param metadataFile The metadata-file to update
	 * @return The updated metadata-file
	 */
	public Metadata updateMetadata(File metadataFile) {
		return MetadataFileHandler.updateMetadata(metadataFile);
	}
	
	/**
	 * Changes the compile directory to directory parameter in project.
	 * @param directory New directory
	 * @param projectFile Project to change directory in
	 * @param internal {@code true} if directory path should be internal, otherwise {@code false}
	 * @return The new directory path
	 */
	public String changeDirectory(File directory, ProjectFile projectFile, boolean internal) {
		return MetadataFileHandler.changeDirectory(directory, projectFile, internal);
	}
	
	/**
	 * Changes the compile directory to directory parameter in project.
	 * @param directory New source path directory
	 * @param projectFile Project to change source path directory in
	 * @param internal {@code true} if source path should be internal, otherwise {@code false}
	 * @return The new source path
	 */
	public String changeSourcepath(File directory, ProjectFile projectFile, boolean internal) {
		return MetadataFileHandler.changeSourcepath(directory, projectFile, internal);
	}
	
	public boolean containMainMethod(File classFile) {
		String content;
		try {
			content = JavaFileHandler.readFile(classFile);
			if (content.contains("public static void main")) {
				return true;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return false;
	}
}
