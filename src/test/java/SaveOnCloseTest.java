import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import main.java.zenit.setup.SetupController;
import main.java.zenit.ui.DialogBoxes;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

public class SaveOnCloseTest {

    private final SetupController setupController = new SetupController();

   //saveOnCloseTest
    @Test
    void testCloseWithUnsavedChanges () {
        try (MockedStatic<DialogBoxes> mockedDialogBoxes = mockStatic(DialogBoxes.class)) {
            mockedDialogBoxes.when(DialogBoxes::unsavedModificationsDialogSimple)
                    .thenReturn(1);

            boolean result = setupController.handleCloseAction();
            assertTrue(result, "Application should close after saving");
        }
    }

    @Test
    void testCloseWithUnsavedChanges_Discard() {
        try (MockedStatic<DialogBoxes> mockedDialogBoxes = mockStatic(DialogBoxes.class)) {
            mockedDialogBoxes.when(DialogBoxes::unsavedModificationsDialogSimple)
                    .thenReturn(2);
            boolean result = setupController.handleCloseAction();
            assertTrue(result, "Application should close after discarding changes");
        }
    }

    @Test
    void testCloseWithUnsavedChanges_Cancel() {
        try (MockedStatic<DialogBoxes> mockedDialogBoxes = mockStatic(DialogBoxes.class)) {
            mockedDialogBoxes.when(DialogBoxes::unsavedModificationsDialogSimple)
                    .thenReturn(3);

            boolean result = setupController.handleCloseAction();
            assertFalse(result, "Application should remain open after canceling the close action");
        }
    }

}