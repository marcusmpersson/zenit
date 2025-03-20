import main.java.zenit.filesystem.FileController;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;


public class WriteFileTest {
        @Test
        public void testWriteFile() {

            File testFile = null;
            try {
                testFile = File.createTempFile("testFile", ".txt");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }


            String content = "Hello, World!";
            boolean result = FileController.writeFile(testFile, content);
            assertEquals(true, result);

            if (testFile.exists()) {
                testFile.delete();
            }

        }
}
