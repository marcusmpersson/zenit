import javafx.application.Platform;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import main.java.zenit.filesystem.FileController;
import main.java.zenit.ui.MainController;
import main.java.zenit.ui.tree.FileTreeItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

public class ChooseExecutableClassTest {
    @Mock
    private File file;


    private FileController fileController;

    @Mock
    private Stage stage;


    @Mock
    private FileTreeItem fileTreeItem;



    private MainController mainController;

    static {
        // Initialize JavaFX toolkit once for all tests
        Platform.startup(() -> {
        });
    }

    @BeforeEach
    public void setUp() {
        stage = mock(Stage.class);
        mainController = new MainController(stage);

        file = new File("test.java");
        if (file.exists()) {
            file.delete();
        }


    }

    @Test
    public void classHasMainMethod() {
        String content = "public static void main";
        file = new File("test.java");

        try (FileWriter writer = new FileWriter(file, true)) {
            writer.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        }

        fileController = new FileController(file);

        boolean result = fileController.containMainMethod(file);
        assertEquals(true, result);
    }


    @Test
    public void classHasNoMainMethod() {
        String content = "public void";
        file = new File("test.java");

        try (FileWriter writer = new FileWriter(file, true)) {
            writer.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        }

        fileController = new FileController(file);

        boolean result = fileController.containMainMethod(file);
        assertEquals(false, result);
    }


    /*
    @Test
    public void mainMethodHasPlayIcon() {
        File file = new File("test.java");
        String name = "Test File";
        int type = 1;
        boolean test = true;


        String content = "public static void main(String[] args)";
        try (FileWriter writer = new FileWriter(file, true)) {
            writer.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        }

        fileController = new FileController(file);

        // Initialize the FileTreeItem object
        fileTreeItem = new FileTreeItem(file, name, type, test);

        // Add the icon if the method is runnable
        fileTreeItem.addIconIfMethodRunnable(file);

        // Retrieve the play icon
        ImageView icon = fileTreeItem.getPlayIcon();

        // Assert that the icon is not null
        assertNotNull(icon, "The play icon should not be null");

        // Clean up the file after the test
        if (file.exists()) {
            file.delete();
        }
    }

     */



}
