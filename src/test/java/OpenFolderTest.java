import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import main.java.zenit.filesystem.FileController;
import main.java.zenit.ui.MainController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.scene.control.TabPane;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class OpenFolderTest {

    @Mock
    private Stage stage;

    @Mock
    private FileController fileController;


    private MainController mainController;

    @Mock
    private TabPane tabPane;
    private AutoCloseable closeable;

    /*
    Before, the JavaFX toolkit was being initialized multiple times,
    which caused an IllegalStateException to be thrown.
    When you run testOpenFile_FileIsNull() alone,
    the toolkit is initialized once, and the test passes.
    However, when you add testOpenFile_FileSupported(),
    the toolkit is initialized again, causing the failure.

    To fix this, we can initialize the JavaFX toolkit once for all tests.
    This can be done by adding a static block to the test class.
     */
    static {
        // Initialize JavaFX toolkit once for all tests
        Platform.startup(() -> {});
    }

    @BeforeEach
    public void setUp() {
        // Initialize mocks
        closeable = MockitoAnnotations.openMocks(this);

        tabPane = new TabPane();
        stage = mock(Stage.class);
        fileController = mock(FileController.class);
        mainController = new MainController(stage, tabPane, fileController);
    }

    @Test
    public void testOpenFile_FileIsNull() {
        mainController.openFile((File) null);

        // Verify that no tabs are added when file is null
        assertEquals(0, tabPane.getTabs().size());
    }

    @Test
    public void testOpenFile_FileSupported() {
        Path tempFile = null;

        // Create a temporary file
        try {
            tempFile = Files.createTempFile("test", ".java");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        File file = tempFile.toFile();

        try {
            mainController.openFile(file);
            assertEquals(1, tabPane.getTabs().size());
        } finally {
            // Delete the temporary file
            try {
                Files.deleteIfExists(tempFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}