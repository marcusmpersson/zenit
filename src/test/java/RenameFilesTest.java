import main.java.zenit.filesystem.FileController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class RenameFilesTest {
    private FileController fileController;

    @BeforeEach
    public void setUp(@TempDir Path tempDir) {
        fileController = new FileController(tempDir.toFile());
    }

    @Test
    public void testRenameFile(@TempDir Path tempDir) {
        File originalFile = new File(tempDir.toFile(), "originalFile.txt");

        boolean created;

        try {
            created = originalFile.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if(created){
            File renamedFile = fileController.renameFile(originalFile, "renamedFile.txt");
            assertEquals("renamedFile.txt", renamedFile.getName());
        }

    }
}