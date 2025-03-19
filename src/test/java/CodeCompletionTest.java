import main.java.zenit.completions.CompletionModule;
import main.java.zenit.completions.CompletionModule.CompletionItem;
import main.java.zenit.zencodearea.ZenCodeArea;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CodeCompletionTest {
    private CompletionModule completionModule;
    private ZenCodeArea codeArea;

    @BeforeEach
    void setUp() {
        codeArea = Mockito.mock(ZenCodeArea.class);

        completionModule = Mockito.mock(CompletionModule.class);
    }

    @Test
    void testBasicCodeCompletions() {
        String inputCode = "System.out.pr";
        int position = inputCode.length();

        Mockito.when(codeArea.getText()).thenReturn(inputCode);
        Mockito.when(codeArea.getCaretPosition()).thenReturn(position);

        List<CompletionItem> fakeCompletions = List.of(
                new CompletionItem("System", "class", null, "java.lang.System"),
                new CompletionItem("println", "method", null, "System.out.println()"),
                new CompletionItem("print", "method", null, "System.out.print()"),
                new CompletionItem("public", "keyword", null, "Java keyword"),
                new CompletionItem("static", "keyword", null, "Java keyword")
        );

        Mockito.when(completionModule.getCompletions(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt()))
                .thenReturn(fakeCompletions);

        List<CompletionItem> completions = completionModule.getCompletions(inputCode, "TestFile.java", position);

        System.out.println("Suggested completions:");
        for (CompletionItem item : completions) {
            System.out.println(" - " + item.getName());
        }

        assertNotNull(completions, "The completion list should not be null");
        assertFalse(completions.isEmpty(), "There should be suggestions");

        boolean containsSystem = completions.stream().anyMatch(item -> item.getName().equals("System"));
        boolean containsPrintln = completions.stream().anyMatch(item -> item.getName().equals("println"));
        boolean containsPrint = completions.stream().anyMatch(item -> item.getName().equals("print"));
        boolean containsPublic = completions.stream().anyMatch(item -> item.getName().equals("public"));
        boolean containsStatic = completions.stream().anyMatch(item -> item.getName().equals("static"));

        assertTrue(containsSystem, "The completion list should contain 'System'");
        assertTrue(containsPrintln, "The completion list should contain 'println'");
        assertTrue(containsPrint, "The completion list should contain 'print'");
        assertTrue(containsPublic, "The completion list should contain 'public'");
        assertTrue(containsStatic, "The completion list should contain 'static'");
    }
}
