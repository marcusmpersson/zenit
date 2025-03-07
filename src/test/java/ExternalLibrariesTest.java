import main.java.zenit.filesystem.FileController;
import main.java.zenit.filesystem.ProjectFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ExternalLibrariesTest {

    @Test
    public void addJarTest(@TempDir Path tmpDir) {
        FileController fileController = new FileController(tmpDir.toFile());
        fileController.createProject("mock-project");
        Path root = tmpDir.resolve("mock-project");
        if (!Files.exists(root)) {
            throw new RuntimeException("Unable to execute test. Could not create temporary directory: " + root);
        }

        List<File> libraries = new ArrayList<>();
        URL url = getClass().getClassLoader().getResource("commons-lang3-3.17.0.jar");
        if (url == null) {
            throw new RuntimeException("Unable to execute test due to missing resources");
        }
        File jarFile = new File(url.getFile());
        libraries.add(jarFile);

        ProjectFile projectFile = new ProjectFile(String.valueOf(root));
        fileController.addInternalLibraries(libraries, projectFile);

        assertAll(
                () -> assertTrue(Files.exists(root.resolve("lib")), "Failed to create /lib directory"),
                () -> assertTrue(Files.exists(root.resolve("lib").resolve(jarFile.getName())), "Failed to add .jar")
        );
    }

    @Test
    public void removeJarTest(@TempDir Path tmpDir) {
        FileController fileController = new FileController(tmpDir.toFile());
        fileController.createProject("mock-project");
        Path root = tmpDir.resolve("mock-project");
        if (!Files.exists(root)) {
            throw new RuntimeException("Unable to execute test. Could not create temporary directory: " + root);
        }

        List<File> libraries = new ArrayList<>();
        URL url = getClass().getClassLoader().getResource("commons-lang3-3.17.0.jar");
        if (url == null) {
            throw new RuntimeException("Unable to execute test due to missing resources");
        }
        File jarFile = new File(url.getFile());
        libraries.add(jarFile);

        ProjectFile projectFile = new ProjectFile(String.valueOf(root));
        fileController.addInternalLibraries(libraries, projectFile);

        assertAll(
                () -> assertTrue(Files.exists(root.resolve("lib")), "Unable to execute test: No /lib directory present"),
                () -> assertTrue(Files.exists(root.resolve("lib").resolve(jarFile.getName())), "Unable to execute test: No .jar-file present")
        );

        List<String> paths = new ArrayList<>();
        String fileSeparator = FileSystems.getDefault().getSeparator();
        String relativePath = "lib" + fileSeparator + jarFile.getName();
        paths.add(relativePath);
        boolean success = fileController.removeInternalLibraries(paths, projectFile);

        assertAll(
                () -> assertTrue(success, "Removal failed"),
                () -> assertFalse(Files.exists(Path.of(relativePath)), "Failed to remove: " + relativePath)
        );
    }
}