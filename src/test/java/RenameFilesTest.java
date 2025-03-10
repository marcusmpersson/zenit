import static org.junit.jupiter.api.Assertions.*;

import main.java.zenit.filesystem.FileController;
import org.junit.jupiter.api.*;
import org.mockito.*;

import java.io.File;
import java.io.IOException;

class RenameFilesTest {

    private FileController fileController;
    private File mockWorkspace;

    @BeforeEach
    void setUp() {
        // Create a mock File to act as the workspace
        mockWorkspace = Mockito.mock(File.class);
        Mockito.when(mockWorkspace.getPath()).thenReturn("/mock/workspace");

        // Pass the mock workspace to FileController
        fileController = new FileController(mockWorkspace);
    }

    @Test
    void testRenameFile_Success() throws IOException {
        // Mock the file to rename
        File mockFile = Mockito.mock(File.class);

        Mockito.when(mockFile.renameTo(Mockito.any())).thenReturn(true);
        Mockito.when(mockFile.getAbsolutePath()).thenReturn("/mock/workspace/oldFile.java");

        // Perform renaming
        File renamedFile = fileController.renameFile(mockFile, "newFile.java");

        // Validate renaming
        assertNotNull(renamedFile, "Renamed file should not be null.");
    }

    @Test
    void testRenameFile_RenameFails() {

        File mockFile = Mockito.mock(File.class);

        Mockito.when(mockFile.getAbsolutePath()).thenReturn("/mock/workspace/oldFile.java");

        // Simulate renameTo method failing
        Mockito.when(mockFile.renameTo(Mockito.any())).thenReturn(false);

        // Expect an IOException when renaming fails
        Exception exception = assertThrows(IOException.class, () -> {
            fileController.renameFile(mockFile, "newFile.java");
        });

        // Verify the correct error message
        assertEquals("Couldn't rename file", exception.getMessage());
    }


    @Test
    void testRenameFile_FileAlreadyExists() {
        // Mock a file that already exists
        File existingFile = Mockito.mock(File.class);
        Mockito.when(existingFile.getAbsolutePath()).thenReturn("/mock/workspace/oldFile.java");
        Mockito.when(existingFile.exists()).thenReturn(true);

        // Expect an IOException when renaming to an existing file name
        Exception exception = assertThrows(IOException.class, () -> {
            fileController.renameFile(existingFile, "existingFile.java");
        });

        // Verify the error message matches what is actually thrown
        assertEquals("Couldn't rename file", exception.getMessage());
    }

}
