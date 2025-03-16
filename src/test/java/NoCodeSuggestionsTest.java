import javafx.application.Platform;
import main.java.zenit.completions.CompletionModule;
import main.java.zenit.zencodearea.ZenCodeArea;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import javax.tools.ToolProvider;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;

//FFE105
public class NoCodeSuggestionsTest {
    private CompletionModule completionModule;

    @Mock
    private ZenCodeArea zenCodeArea;

    private String fileName;

    String sourceCode;

    @BeforeAll
    public static void initToolkit() {
        // Initializes the JavaFX toolkit
        Platform.startup(() -> {});
    }

    @BeforeEach
    public void setUp() {
        zenCodeArea = new ZenCodeArea();
        completionModule = new CompletionModule(zenCodeArea);
        fileName = "Testfile.java";
        completionModule.setCurrentFileName(fileName);
        sourceCode = "aaaaa";
    }

    @Test
    public void testNoCodeSuggestions() {
        List<CompletionModule.CompletionItem> completions = completionModule.getCompletions(sourceCode, fileName, 3);

        assertTrue(completions == null || completions.isEmpty(), "The popup should not appear when no suggestions are available.");
    }

    @Test
    public void javaCompilerAPIError() {
        // Mock the ToolProvider to return null for the system Java compiler
        try (MockedStatic<ToolProvider> mockedToolProvider = Mockito.mockStatic(ToolProvider.class)) {
            mockedToolProvider.when(ToolProvider::getSystemJavaCompiler).thenReturn(null);

            List<CompletionModule.CompletionItem> completions = completionModule.getCompletions(sourceCode, fileName, 3);

            assertTrue(completions == null || completions.isEmpty(), "The java compiler API is not reachable");
        }
    }


}