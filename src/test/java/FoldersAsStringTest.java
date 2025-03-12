import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.io.File;
import main.java.zenit.filesystem.helpers.FileNameHelpers;

public class FoldersAsStringTest {

        @Test
        public void testGetFoldersAsStringArray() {

            File file1 = new File("C:/Users/Username/Projects/ProjectName/src/main/java");
            String[] expected1 = {"C:", "Users", "Username", "Projects", "ProjectName", "src", "main", "java"};

            String[] result = FileNameHelpers.getFoldersAsStringArray(file1);

            assertArrayEquals(expected1, result);

        }

}
