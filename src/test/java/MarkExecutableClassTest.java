import javafx.scene.image.ImageView;
import main.java.zenit.ui.tree.FileTreeItem;
import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class MarkExecutableClassTest{

    @Test
    void testPlayIconOnExecutableClass() throws Exception {
        //creates a temporary file with main method
        Path tempFile = Files.createTempFile("TestClass", ".java");
        String javaCode = "public class TestClass {\n"
                + "    public static void main(String[] args) {\n"
                + "        System.out.println(\"Hello World\");\n"
                + "    }\n"
                + "}";
        Files.write(tempFile, javaCode.getBytes(), StandardOpenOption.WRITE);

        //creates a FileTreeItem object with the temporary file
        //spy is used to mock the getPlayIcon method
        FileTreeItem<String> fileTreeItem = spy(new FileTreeItem<>(tempFile.toFile(), tempFile.getFileName().toString(), FileTreeItem.CLASS, true));

        //mocks the getPlayIcon method
        ImageView playIconMock = mock(ImageView.class);
        doReturn(playIconMock).when(fileTreeItem).getPlayIcon();

        //asserts that the play icon is present for executable Java classes
        assertNotNull(fileTreeItem.getPlayIcon(), "The play icon should be present for executable Java classes");

        //deletes the temporary file
        Files.delete(tempFile);
    }
}