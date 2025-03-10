package main.java.zenit.filesystem;

import main.java.zenit.exceptions.TypeCodeException;
import main.java.zenit.filesystem.helpers.CodeSnippets;
import main.java.zenit.filesystem.helpers.FileNameHelpers;
import main.java.zenit.ui.NewFileController;

import java.io.*;
import java.util.logging.Logger;

/**
 * Methods for creating, reading, writing, renaming and deleting .java files in file system.
 * Only to be accessed using {@link FileController FileController} methods.
 *
 * @author Alexander Libot
 *
 */
public class JavaFileHandler extends FileHandler {

	private static final Logger logger = Logger.getLogger(JavaFileHandler.class.getName());

	/**
	 * Tries to create a new .java file in file system. Adds .java to file name if not already
	 * added. If {@code content} is {@code null}, adds a code snippet from
	 * {@link zenit.filesystem.helpers.CodeSnippets CodeSnippets} using {@code typeCode}
	 * parameter. If content is not {@code null}, writes the data from {@code content} to file.
	 *
	 * @param file File to be created
	 * @param content Content to be written to file. May be null.
	 * @param typeCode If {@code content} is null, writes content from {@link
	 * zenit.filesystem.helpers.CodeSnippets CodeSnippets} using this parameter.
	 * @return Created file if created, otherwise {@link java.io.IOException IOException} is thrown
	 * @throws IOException Throws IOException if file already exists or it couldn't
	 * be created.
	 */
	protected static File createFile(File file, String content, int typeCode) throws IOException {
		try {
			String fileName = file.getName();
			// Adds .java if not already added
			if (!fileName.endsWith(".java")) {
				String filepath = file.getPath() + ".java";
				file = new File(filepath);
			}

			// Checks if file already exists
			if (file.exists()) {
				throw new IOException("File already exists");
			}

			// Tries to create file
			file.createNewFile();

			// Write content to file
			if (content == null) {
				try {
					content = CodeSnippets.newSnippet(typeCode, file.getName(), FileNameHelpers.getPackagenameFromFile(file));
				} catch (TypeCodeException ex) {
					logger.severe("TypeCodeException occurred while creating content: " + ex.getMessage());
				}
			}

			try (BufferedWriter br = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), textEncoding))) {
				br.write(content);
				br.flush();
			}

			return file;
		} catch (IOException e) {
			logger.severe("IOException occurred: " + e.getMessage());
			throw new IOException("Couldn't create new class");
		}
	}

	/**
	 * Tries to read file from disk.
	 * @param file The file to read
	 * @return Returns String-object with content from {@code file} if read, otherwise
	 * throws a {@link java.io.IOException IOException}
	 * @throws IOException Throw IOException if file could not be read.
	 */
	protected static String readFile(File file) throws IOException {
		try (BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(file), textEncoding))) {

			StringBuilder stringBuilder = new StringBuilder();
			String currentString = br.readLine();

			while (currentString != null) {
				stringBuilder.append(currentString).append("\n");
				currentString = br.readLine();
			}
			return stringBuilder.toString();

		} catch (IOException ex) {
			logger.severe("File couldn't be read: " + ex.getMessage());
			throw new IOException("File couldn't be read");
		}
	}

	/**
	 * Tries to save {@code content} to disk in {@code file}.
	 * @param file The file to write over with {@code content}.
	 * @param content The new content of the file
	 * @throws IOException Throws {@link java.io.IOException IOException} if file
	 * can't be saved.
	 */
	protected static void saveFile(File file, String content) throws IOException {
		try (BufferedWriter br = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(file), textEncoding))) {
			br.write(content);
			br.flush();
		} catch (IOException ex) {
			logger.severe("Failed to save file: " + ex.getMessage());
			throw new IOException(ex.getMessage());
		}
	}

	/**
	 * Tries to rename the file.
	 * @param oldFile File to be renamed
	 * @param newFilename The new filename
	 * @return The renamed file
	 * @throws IOException Throws IOException if file already exists with same name or
	 * if file couldn't be renamed
	 */
	protected static File renameFile(File oldFile, String newFilename) throws IOException {


		File tempFile = FileNameHelpers.getFilepathWithoutTopFile(oldFile); // Removes file name

		// Create new file with new name
		String newFilepath = tempFile.getPath() + "/" + newFilename;
		File newFile = new File(newFilepath);

		if (newFile.exists()) {
			NewFileController.renameFile(newFilename);
			throw new IOException("File already exists");
		}

		boolean success = oldFile.renameTo(newFile);

		if (!success) {
			throw new IOException("Couldn't rename file");
		}

		return newFile;
	}

	/**
	 * Tries to delete the file.
	 * @param file The file to delete
	 * @throws IOException Throws {@link java.io.IOException IOException} if file can't
	 * be deleted.
	 */
	protected static void deleteFile(File file) throws IOException {
		if (!file.delete()) {
			logger.severe("Failed to delete file: " + file.getPath());
			throw new IOException("Failed to delete " + file);
		}
	}
}
