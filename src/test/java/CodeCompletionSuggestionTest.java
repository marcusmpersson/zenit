import main.java.zenit.completions.CompletionModule;
import main.java.zenit.completions.CompletionModule.CompletionItem;
import main.java.zenit.zencodearea.ZenCodeArea;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CodeCompletionSuggestionTest {
    private CompletionModule completionModule;
    private ZenCodeArea codeArea;

    private static boolean javafxStarted = false;

    @BeforeAll
    static void initJFX() throws Exception {
        if (!javafxStarted) {
            CountDownLatch latch = new CountDownLatch(1);

            new Thread(() -> {
                try {
                    Application.launch(TestApp.class);
                } catch (IllegalStateException e) {
                }
                latch.countDown();
            }).start();

            latch.await(5, TimeUnit.SECONDS);
            javafxStarted = true;
        }
    }

    @BeforeEach
    void setUp() {
        Platform.runLater(() -> {
            codeArea = Mockito.mock(ZenCodeArea.class);
            completionModule = new CompletionModule(codeArea);
        });
        sleep(500);
    }

    @Test
    void testSuggestionsAreGenerated() throws Exception {
        Platform.runLater(() -> {
            String inputCode = "sys";
            when(codeArea.getText()).thenReturn(inputCode);
            when(codeArea.getCaretPosition()).thenReturn(inputCode.length());

            completionModule.showCompletions();

            ListView<CompletionItem> completionList = null;
            try {
                completionList = (ListView<CompletionItem>) getPrivateField(completionModule, "completionList");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            List<CompletionItem> suggestions = completionList.getItems();

            // Säkerställ att listan inte är tom
            assertNotNull(suggestions, "Completion list should not be null");
            assertFalse(suggestions.isEmpty(), "Completion list should contain suggestions");

            // Kontrollera att "System" är ett av förslagen
            boolean containsSystem = suggestions.stream()
                    .anyMatch(item -> item.getName().equals("System"));

            assertTrue(containsSystem, "Completion should suggest 'System'");
        });
        sleep(500);
    }

    public static class TestApp extends Application {
        @Override
        public void start(Stage primaryStage) {
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T getPrivateField(Object instance, String fieldName) throws Exception {
        Field field = instance.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return (T) field.get(instance);
    }

    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {
        }
    }
}
