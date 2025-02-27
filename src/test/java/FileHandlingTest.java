import main.java.zenit.filesystem.FileController;
import main.java.zenit.filesystem.helpers.CodeSnippets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class FileHandlingTest {
    FileController fileController;

    public Path mockProject(@TempDir Path tmpDir) {
        fileController = new FileController(tmpDir.toFile());
        fileController.createProject("mock-project");
        return tmpDir.resolve("mock-project");
    }

    public File mockFile(Path dir) {
        try {
            File mockFile = new File(dir.toFile(), "MockClass.java");

            try (FileWriter writer = new FileWriter(mockFile)) {
                writer.write("public class MockClass {\n");
                writer.write("    public static void main(String[] args) {\n");
                writer.write("    }\n");
                writer.write("}\n");
            }

            return mockFile;
        } catch (IOException e) {
            throw new RuntimeException("Unable to execute test. Failed write operation: " + e);
        }
    }

    @Test
    public void testCreateClass(@TempDir Path tmpDir) {
        Path root = mockProject(tmpDir);
        Path src = root.resolve("src");
        File newClass = new File(src + File.separator + "Class");

        fileController.createFile(newClass, CodeSnippets.CLASS);

        assertTrue(Files.exists(root.resolve("src").resolve("Class.java")), "Failed to create class: " + newClass);
    }

    @Test
    public void testCreateInterface(@TempDir Path tmpDir) {
        Path root = mockProject(tmpDir);
        Path src = root.resolve("src");
        File newInterface = new File(src + File.separator + "Interface");

        fileController.createFile(newInterface, CodeSnippets.INTERFACE);

        assertTrue(Files.exists(root.resolve("src").resolve("Interface.java")), "Failed to create interface: " + newInterface);
    }

    @Test
    public void testCreateDirectory(@TempDir Path tmpDir) {
        Path root = mockProject(tmpDir);
        Path src = root.resolve("src");
        File newDirectory = new File(src + File.separator + "testPackage");

        fileController.createPackage(newDirectory);

        assertAll(
                () -> assertTrue(Files.exists(root.resolve("src").resolve("testPackage"))),
                () -> assertTrue(Files.isDirectory(root.resolve("src").resolve("testPackage")))
        );
    }

    @Test
    public void testDeleteFile(@TempDir Path tmpDir) {
        Path root = mockProject(tmpDir);
        Path src = root.resolve("src");
        File newClass = new File(src + File.separator + "Class");
        fileController.createFile(newClass, CodeSnippets.CLASS);

        Path fileToDelete = root.resolve("src").resolve("Class.java");
        assert Files.exists(fileToDelete);
        fileController.deleteFile(fileToDelete.toFile());

        assertFalse(Files.exists(fileToDelete), "Failed to delete file: " + fileToDelete);
    }

    @Test
    public void testDeletePackage(@TempDir Path tmpDir) {
        Path root = mockProject(tmpDir);
        Path src = root.resolve("src");
        File newDirectory = new File(src + File.separator + "testPackage");
        fileController.createPackage(newDirectory);

        Path dirToDelete = root.resolve("src").resolve("testPackage");
        assert Files.exists(dirToDelete);
        fileController.deleteFile(dirToDelete.toFile());

        assertFalse(Files.exists(dirToDelete), "Failed to delete package: " + dirToDelete);
    }

    @Test
    public void testWriteToFile(@TempDir Path tmpDir) {
        Path root = mockProject(tmpDir);
        File file = mockFile(root);
        String content =
                "public class MockClass {\n" +
                        "    public static void main(String[] args) {\n" +
                        "        System.out.println(\"This is a mock\");\n" +
                        "    }\n" +
                        "}\n";

        FileController.writeFile(file, content);

        assertAll(
                () -> assertTrue(file.exists(), "Failed to create file: " + file),
                () -> assertEquals(content, Files.readString(file.toPath()), "Actual content does not match expected content")
        );
    }

    @Test
    public void testReadFromFile(@TempDir Path tmpDir) {
        Path root = mockProject(tmpDir);
        File file = mockFile(root);
        String content =
                "public class MockClass {\n" +
                        "    public static void main(String[] args) {\n" +
                        "        System.out.println(\"This is a mock\");\n" +
                        "    }\n" +
                        "}\n";

        FileController.writeFile(file, content);

        String read = FileController.readFile(file);

        String expectedNormalized = content.replace("\r\n", "\n").trim();
        String actualNormalized = read.replace("\r\n", "\n").trim();
        
        assertAll(
                () -> assertNotNull(read, "Read content should not be null"),
                () -> assertFalse(read.isEmpty(), "Read content should not be empty"),
                () -> assertEquals(expectedNormalized, actualNormalized, "File content should match expected content")
        );
    }
}
