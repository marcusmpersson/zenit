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
import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CodeCompletionSelectionTest {
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
    void testNavigateSuggestionsWithArrowKeys() throws Exception {
        realCompletionList.getItems().addAll(
                new CompletionItem("System", "class", null, ""),
                new CompletionItem("String", "class", null, ""),
                new CompletionItem("Scanner", "class", null, "")
        );

        realCompletionList.getSelectionModel().select(0);
        invokePrivateMethod(completionModule, "navigateCompletions", 1);
        assertEquals("String", realCompletionList.getSelectionModel().getSelectedItem().getName());

        invokePrivateMethod(completionModule, "navigateCompletions", 1);
        assertEquals("Scanner", realCompletionList.getSelectionModel().getSelectedItem().getName());

        invokePrivateMethod(completionModule, "navigateCompletions", -1);
        assertEquals("String", realCompletionList.getSelectionModel().getSelectedItem().getName());
    }

    @Test
    void testApplySelectedCompletionWithCtrlTab() throws Exception {
        realCompletionList.getItems().addAll(
                new CompletionItem("System", "class", null, ""),
                new CompletionItem("String", "class", null, ""),
                new CompletionItem("Scanner", "class", null, "")
        );

        realCompletionList.getSelectionModel().select(1);

        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            try {
                invokePrivateMethod(completionModule, "applySelectedCompletion");
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                latch.countDown();
            }
        });

        latch.await();

        CompletionItem selectedItem = realCompletionList.getSelectionModel().getSelectedItem();
        assertNotNull(selectedItem, "No item selected after ctrl + tab was pressed");
        assertEquals("String", selectedItem.getName(), "Wrong item selected after ctrl + tab was pressed");
    }


    private void setPrivateField(Object instance, String fieldName, Object value) throws Exception {
        Field field = instance.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(instance, value);
    }

    private void invokePrivateMethod(Object instance, String methodName, Object... args) throws Exception {
        Method method = instance.getClass().getDeclaredMethod(methodName, int.class);
        method.setAccessible(true);
        method.invoke(instance, args);
    }

    private void invokePrivateMethod(Object instance, String methodName) throws Exception {
        Method method = instance.getClass().getDeclaredMethod(methodName);
        method.setAccessible(true);
        method.invoke(instance);
    }
}
