import javafx.application.Platform;
import javafx.scene.control.ListView;
import main.java.zenit.completions.CompletionModule;
import main.java.zenit.completions.CompletionModule.CompletionItem;
import main.java.zenit.zencodearea.ZenCodeArea;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import static org.junit.jupiter.api.Assertions.*;

public class CodeCompletionTypesTest {
    private CompletionModule completionModule;
    private ZenCodeArea codeArea;
    private ListView<CompletionItem> realCompletionList;

    @BeforeAll
    static void initJFX() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.startup(latch::countDown);
        latch.await();
    }

    @BeforeEach
    void setUp() throws Exception {
        codeArea = Mockito.mock(ZenCodeArea.class);
        completionModule = new CompletionModule(codeArea);

        realCompletionList = new ListView<>();
        setPrivateField(completionModule, "completionList", realCompletionList);
    }

    @Test
    void testCompletionTypes() throws Exception {
        realCompletionList.getItems().addAll(
                new CompletionItem("System", "class", null, ""),
                new CompletionItem("println", "method", null, ""),
                new CompletionItem("out", "field", null, ""),
                new CompletionItem("public", "keyword", null, ""),
                new CompletionItem("static", "keyword", null, ""),
                new CompletionItem("void", "keyword", null, "")
        );

        // förslagslistan
        List<CompletionItem> suggestions = realCompletionList.getItems();

        assertTrue(suggestions.stream().anyMatch(item -> item.getName().equals("System") && item.getType().equals("class")), "Expected class missing");
        assertTrue(suggestions.stream().anyMatch(item -> item.getName().equals("println") && item.getType().equals("method")), "Expected method missing");
        assertTrue(suggestions.stream().anyMatch(item -> item.getName().equals("out") && item.getType().equals("field")), "Expected field missing");
        assertTrue(suggestions.stream().anyMatch(item -> item.getName().equals("public") && item.getType().equals("keyword")), "Expected keyword missing");
        assertTrue(suggestions.stream().anyMatch(item -> item.getName().equals("static") && item.getType().equals("keyword")), "Expected keyword missing");
        assertTrue(suggestions.stream().anyMatch(item -> item.getName().equals("void") && item.getType().equals("keyword")), "Expected keyword missing");
    }

    private void setPrivateField(Object instance, String fieldName, Object value) throws Exception {
        Field field = instance.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(instance, value);
    }
}
