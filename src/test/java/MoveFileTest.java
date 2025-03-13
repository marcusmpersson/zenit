import org.junit.jupiter.api.Test;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import static org.junit.jupiter.api.Assertions.*;

class MoveFileTest {

    @Test
    void testMoveFileOrFolder() throws IOException {
        File sourceFolder = new File("temp/source");
        File destinationFolder = new File("temp/destination");
        sourceFolder.mkdirs();
        destinationFolder.mkdirs();

        File fileToMove = new File(sourceFolder, "TestClass.java");
        String originalContent = "public class TestClass { public static void main(String[] args) {} }";
        Files.writeString(fileToMove.toPath(), originalContent);

        assertTrue(fileToMove.exists(), "Source file should exist before moving");

        Files.move(fileToMove.toPath(),
                destinationFolder.toPath().resolve(fileToMove.getName()),
                StandardCopyOption.REPLACE_EXISTING);

        File movedFile = new File(destinationFolder, "TestClass.java");

        assertAll(
                () -> assertFalse(fileToMove.exists(), "Source file should not exist after moving"),
                () -> assertTrue(movedFile.exists(), "File should exist in the destination after moving"),
                () -> assertEquals(originalContent, Files.readString(movedFile.toPath()), "Content should remain unchanged after moving")
        );

        deleteDirectoryRecursively(new File("temp"));
    }

    @Test
    void testMovedJavaFileCompilable() throws IOException {
        File sourceFolder = new File("temp/source");
        File destinationFolder = new File("temp/destination");
        sourceFolder.mkdirs();
        destinationFolder.mkdirs();

        File fileToMove = new File(sourceFolder, "TestClass.java");
        String originalContent = "public class TestClass { public static void main(String[] args) {} }";
        Files.writeString(fileToMove.toPath(), originalContent);

        Files.move(fileToMove.toPath(),
                destinationFolder.toPath().resolve(fileToMove.getName()),
                StandardCopyOption.REPLACE_EXISTING);

        File movedFile = new File(destinationFolder, "TestClass.java");

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        int compilationResult = compiler.run(null, null, null, movedFile.getAbsolutePath());

        assertEquals(0, compilationResult, "Java file should compile successfully after moving");

        Files.deleteIfExists(new File("TestClass.class").toPath());
        deleteDirectoryRecursively(new File("temp"));
    }

    void deleteDirectoryRecursively(File directory) throws IOException {
        if (directory.isDirectory()) {
            File[] entries = directory.listFiles();
            if (entries != null) {
                for (File entry : entries) {
                    deleteDirectoryRecursively(entry);
                }
            }
        }
        Files.deleteIfExists(directory.toPath());
    }
}
