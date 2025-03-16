import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import main.java.zenit.completions.CompletionModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.*;

public class CodeCompletionsTriggerTest {
    private CompletionModule completionModule;

    @BeforeEach
    void setUp() {
        // Skapa en mockad CompletionModule utan att köra JavaFX
        completionModule = Mockito.mock(CompletionModule.class);
    }

    @Test
    void testTriggerCompletionOnCtrlSpace() {
        // Simulera att användaren trycker Ctrl + Space
        KeyEvent ctrlSpaceEvent = new KeyEvent(
                KeyEvent.KEY_PRESSED, "", "", KeyCode.SPACE, false, true, false, false
        );

        completionModule.showCompletions();
        verify(completionModule, times(1)).showCompletions();
    }

    @Test
    void testNoCompletionOnOtherKeys() {
        KeyEvent aKeyEvent = new KeyEvent(
                KeyEvent.KEY_PRESSED, "", "", KeyCode.A, false, false, false, false
        );
        verify(completionModule, never()).showCompletions();
    }
}
