package main.java.zenit.completions;

import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import main.java.zenit.zencodearea.ZenCodeArea;

import javax.lang.model.element.*;
import javax.lang.model.element.Modifier;
import javax.tools.*;
import java.lang.reflect.*;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

public class CompletionModule {
    private final ZenCodeArea codeArea;
    private final Popup completionPopup;
    private final ListView<CompletionItem> completionList;
    private final Label completionDetailLabel;

    private String currentFileName = "Placeholder.java";
    private int replacementStart = 0;

    private final double CHAR_WIDTH = 8.0;
    private final double LINE_HEIGHT = 16.0;

    public CompletionModule(ZenCodeArea codeArea) {
        this.codeArea = codeArea;

        this.completionList = new ListView<>();
        this.completionList.setPrefWidth(350);
        this.completionList.setPrefHeight(200);

        this.completionDetailLabel = new Label();
        this.completionDetailLabel.setStyle("-fx-background-color: #726e6e; -fx-padding: 5; -fx-border-color: #424040;");
        this.completionDetailLabel.setPrefWidth(350);

        VBox popupContent = new VBox(completionList, completionDetailLabel);

        this.completionPopup = new Popup();
        this.completionPopup.getContent().add(popupContent);
        this.completionPopup.setAutoHide(true);

        setupEventHandlers();
    }

    public void setCurrentFileName(String fileName) {
        this.currentFileName = fileName;
    }

    private void setupEventHandlers() {
        codeArea.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.isControlDown() && event.getCode() == KeyCode.SPACE) {
                showCompletions();
                event.consume();
                return;
            }

            if (completionPopup.isShowing()) {
                switch (event.getCode()) {
                    case UP:
                        navigateCompletions(-1);
                        event.consume();
                        break;
                    case DOWN:
                        navigateCompletions(1);
                        event.consume();
                        break;
                    case ENTER:
                        if(event.isControlDown()) {
                            applySelectedCompletion();
                            event.consume();
                        }
                    case ESCAPE:
                        completionPopup.hide();
                        event.consume();
                        break;
                    case TAB:
                        if(event.isControlDown()) {
                            applySelectedCompletion();
                            event.consume();
                        }
                        break;
                    default:
                        if (isTextModifyingKey(event) && !event.isControlDown()) {
                            Platform.runLater(this::updateCompletions);
                        }
                        break;
                }
            }

            if (event.getCode() == KeyCode.PERIOD && !event.isControlDown()) {
                Platform.runLater(() -> {
                    try {
                        Thread.sleep(50);
                        Platform.runLater(this::showCompletions);
                    } catch (InterruptedException e) {
                        System.out.println("Interrupted while waiting for dot");
                    }
                });
            }
        });

        completionList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                applySelectedCompletion();
            }
        });

        completionList.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        completionDetailLabel.setText(newValue.getDetail());
                    }
                }
        );
    }

    private boolean isTextModifyingKey(KeyEvent event) {
        return !event.isControlDown() && !event.isAltDown() &&
                event.getCode() != KeyCode.SHIFT &&
                event.getCode() != KeyCode.UP &&
                event.getCode() != KeyCode.DOWN &&
                event.getCode() != KeyCode.LEFT &&
                event.getCode() != KeyCode.RIGHT;
    }

    public void showCompletions() {
        String text = codeArea.getText();
        int caretPosition = codeArea.getCaretPosition();

        List<CompletionItem> completions = getCompletions(text, currentFileName, caretPosition);

        if (completions != null && !completions.isEmpty()) {

            replacementStart = findTokenStart(text, caretPosition);

            completionList.getItems().clear();
            completionList.getItems().addAll(completions);

            completionList.getSelectionModel().select(0);

            showPopupAtCaret();

            Platform.runLater(codeArea::requestFocus);
        }
    }

    private void updateCompletions() {
        if (!completionPopup.isShowing()) {
            return;
        }

        String text = codeArea.getText();
        int caretPosition = codeArea.getCaretPosition();

        if (caretPosition >= replacementStart) {
            List<CompletionItem> completions = getCompletions(text, currentFileName, caretPosition);

            if (completions != null && !completions.isEmpty()) {
                completionList.getItems().clear();
                completionList.getItems().addAll(completions);
                completionList.getSelectionModel().select(0);

                showPopupAtCaret();
            } else {
                completionPopup.hide();
            }
        } else {
            completionPopup.hide();
        }
    }

    private void showPopupAtCaret() {
        try {
            int caretPosition = codeArea.getCaretPosition();
            String text = codeArea.getText();

            int line = 0;
            int column = 0;
            for (int i = 0; i < caretPosition; i++) {
                if (i < text.length() && text.charAt(i) == '\n') {
                    line++;
                    column = 0;
                } else {
                    column++;
                }
            }

            try {
                Method getCaretBoundsMethod = codeArea.getClass().getMethod("getCaretBounds", int.class);
                Object bounds = getCaretBoundsMethod.invoke(codeArea, caretPosition);

                Method getMinXMethod = bounds.getClass().getMethod("getMinX");
                Method getMaxYMethod = bounds.getClass().getMethod("getMaxY");

                double minX = (double) getMinXMethod.invoke(bounds);
                double maxY = (double) getMaxYMethod.invoke(bounds);

                Point2D screenPos = codeArea.localToScreen(minX, maxY);
                completionPopup.show(codeArea, screenPos.getX(), screenPos.getY());
                return;
            } catch (Exception e) {
                System.out.println("Failed to use getCaretBounds");
            }

            Point2D screenPos = codeArea.localToScreen(
                    codeArea.getInsets().getLeft() + (column * CHAR_WIDTH),
                    codeArea.getInsets().getTop() + ((line + 1) * LINE_HEIGHT)
            );

            completionPopup.show(codeArea, screenPos.getX(), screenPos.getY());

            double screenHeight = javafx.stage.Screen.getPrimary().getVisualBounds().getHeight();
            if (screenPos.getY() + completionPopup.getHeight() > screenHeight) {
                completionPopup.setY(screenPos.getY() - completionPopup.getHeight() - LINE_HEIGHT);
            }
        } catch (Exception e) {
            Point2D screenPos = codeArea.localToScreen(50, 50);
            completionPopup.show(codeArea, screenPos.getX(), screenPos.getY());
        }
    }

    private void navigateCompletions(int direction) {
        int currentIndex = completionList.getSelectionModel().getSelectedIndex();
        int newIndex = currentIndex + direction;

        if (newIndex >= 0 && newIndex < completionList.getItems().size()) {
            completionList.getSelectionModel().select(newIndex);
            completionList.scrollTo(newIndex);
        }
    }

    private void applySelectedCompletion() {
        CompletionItem selected = completionList.getSelectionModel().getSelectedItem();

        if (selected != null) {
            String insertText = selected.getName();

            if (selected.getKind() == ElementKind.METHOD) {
                insertText = insertText + "()";
            }

            int caretPosition = codeArea.getCaretPosition();
            codeArea.replaceText(replacementStart, caretPosition, insertText);

            if (selected.getKind() == ElementKind.METHOD) {
                codeArea.moveTo(replacementStart + selected.getName().length() + 1);
            }
        }

        completionPopup.hide();
        codeArea.requestFocus();
    }

    private int findTokenStart(String text, int position) {
        int start = position;

        while (start > 0) {
            char c = text.charAt(start - 1);
            if (!Character.isJavaIdentifierPart(c)) {
                break;
            }
            start--;
        }

        return start;
    }

    public List<CompletionItem> getCompletions(String sourceCode, String fileName, int position) {
        try {
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            if (compiler == null) {
                return getFallbackCompletions(sourceCode, position);
            }

            DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
            StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);

            JavaFileObject sourceFile = new SimpleJavaFileObject(
                    URI.create("string:///" + fileName.replace('.', '/') + ".java"),
                    JavaFileObject.Kind.SOURCE) {
                @Override
                public CharSequence getCharContent(boolean ignoreEncodingErrors) {
                    return sourceCode;
                }
            };

            List<String> options = Arrays.asList(
                    "-proc:none",
                    "-classpath", System.getProperty("java.class.path")
            );

            JavaCompiler.CompilationTask task = compiler.getTask(
                    null, fileManager, diagnostics, options, null, Arrays.asList(sourceFile));

            javax.lang.model.util.Elements elements = null;

            try {
                Method getElementsMethod = task.getClass().getMethod("getElements");
                elements = (javax.lang.model.util.Elements) getElementsMethod.invoke(task);
            } catch (Exception e) {
                return getFallbackCompletions(sourceCode, position);
            }

            CompletionContext context = findCompletionContext(sourceCode, position);
            return generateCompletions(elements, context);

        } catch (Exception e) {
            e.printStackTrace();
            return getFallbackCompletions(sourceCode, position);
        }
    }

    private List<CompletionItem> getFallbackCompletions(String sourceCode, int position) {
        List<CompletionItem> results = new ArrayList<>();

        String[] keywords = {
                "abstract", "assert", "boolean", "break", "byte", "case",
                "catch", "char", "class", "const", "continue", "default",
                "do", "double", "else", "enum", "extends", "final",
                "finally", "float", "for", "if", "implements", "import",
                "instanceof", "int", "interface", "long", "native", "new",
                "package", "private", "protected", "public", "return",
                "short", "static", "strictfp", "super", "switch",
                "synchronized", "this", "throw", "throws", "transient",
                "try", "void", "volatile", "while", "true", "false", "null"
        };

        String prefix = getTokenAtCaret(sourceCode, position);

        for (String keyword : keywords) {
            if (keyword.startsWith(prefix)) {
                results.add(new CompletionItem(keyword, "keyword", ElementKind.OTHER, ""));
            }
        }

        for (String snippet : codeArea.codeSuggestionArray()) {
            if (snippet.startsWith(prefix)) {
                results.add(new CompletionItem(snippet, "snippet", ElementKind.OTHER, "Code snippet"));
            }
        }

        String[] commonClasses = {
                "String", "Integer", "Boolean", "List", "ArrayList", "Map", "HashMap",
                "Set", "HashSet", "File", "Scanner", "System", "Math"
        };

        for (String className : commonClasses) {
            if (className.startsWith(prefix)) {
                results.add(new CompletionItem(className, "class", ElementKind.CLASS, "Common Java class"));
            }
        }

        return results;
    }

    private String getTokenAtCaret(String text, int position) {
        int start = findTokenStart(text, position);
        return text.substring(start, position);
    }

    private CompletionContext findCompletionContext(String source, int position) {
        int start = position;
        while (start > 0 && Character.isJavaIdentifierPart(source.charAt(start - 1))) {
            start--;
        }
        String partial = position > start ? source.substring(start, position) : "";

        int dotPosition = start > 0 ? source.lastIndexOf('.', start - 1) : -1;
        if (dotPosition >= 0) {
            int exprStart = dotPosition;
            while (exprStart > 0) {
                char c = source.charAt(exprStart - 1);
                if (!Character.isJavaIdentifierPart(c) && c != '.' && c != ')' && c != ']') {
                    break;
                }
                exprStart--;
            }
            String expr = source.substring(exprStart, dotPosition);

            boolean isLikelyType = Character.isUpperCase(expr.charAt(0)) && !expr.contains("(");
            CompletionType type = isLikelyType ? CompletionType.STATIC_MEMBER : CompletionType.INSTANCE_MEMBER;

            return new CompletionContext(expr, partial, type, dotPosition, position);
        }

        int lineStart = source.lastIndexOf('\n', position);
        if (lineStart < 0) lineStart = 0;
        String line = source.substring(lineStart, position);
        if (line.trim().startsWith("import ")) {
            return new CompletionContext("", partial, CompletionType.IMPORT, start, position);
        }

        return new CompletionContext("", partial, CompletionType.SIMPLE_NAME, start, position);
    }

    private List<CompletionItem> generateCompletions(javax.lang.model.util.Elements elements, CompletionContext context) {
        List<CompletionItem> results = new ArrayList<>();

        switch (context.type) {
            case INSTANCE_MEMBER:
            case STATIC_MEMBER:
                results.addAll(findMemberCompletions(elements, context, context.type == CompletionType.STATIC_MEMBER));
                break;

            case IMPORT:
                results.addAll(findImportCompletions(context));
                break;

            case SIMPLE_NAME:
                results.addAll(findLocalCompletions(elements));
                break;
        }

        if (!context.partial.isEmpty()) {
            String partial = context.partial.toLowerCase();
            return results.stream()
                    .filter(item -> item.getName().toLowerCase().startsWith(partial))
                    .collect(Collectors.toList());
        }

        return results;
    }

    private List<CompletionItem> findMemberCompletions(javax.lang.model.util.Elements elements, CompletionContext context, boolean staticOnly) {
        List<CompletionItem> results = new ArrayList<>();

        try {
            TypeElement typeElement = findTypeElement(elements, context.prefix);

            if (typeElement != null) {
                for (Element member : elements.getAllMembers(typeElement)) {
                    if (shouldIncludeMember(member, staticOnly)) {
                        String type = member.asType().toString();
                        String detail = getElementDetail(member);

                        results.add(new CompletionItem(
                                member.getSimpleName().toString(),
                                type,
                                member.getKind(),
                                detail
                        ));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return results;
    }

    private TypeElement findTypeElement(javax.lang.model.util.Elements elements, String name) {
        try {
            TypeElement element = elements.getTypeElement(name);
            if (element != null) {
                return element;
            }

            String[] commonPackages = {"java.util.", "java.lang.", "java.io."};
            for (String pkg : commonPackages) {
                element = elements.getTypeElement(pkg + name);
                if (element != null) {
                    return element;
                }
            }

            return null;
        } catch (Exception e) {
            return null;
        }
    }


    private boolean shouldIncludeMember(Element element, boolean staticOnly) {
        if (element.getSimpleName().toString().contains("$")) {
            return false;
        }

        if (staticOnly && !element.getModifiers().contains(Modifier.STATIC)) {
            return false;
        }

        Set<Modifier> modifiers = element.getModifiers();
        if (modifiers.contains(Modifier.PRIVATE)) {
            return false;
        }

        return true;
    }

    private String getElementDetail(Element element) {
        if (element.getKind() == ElementKind.METHOD) {
            ExecutableElement method = (ExecutableElement) element;
            StringBuilder sb = new StringBuilder();
            sb.append(method.getSimpleName());
            sb.append("(");

            boolean first = true;
            for (VariableElement param : method.getParameters()) {
                if (!first) sb.append(", ");
                sb.append(param.asType().toString());
                sb.append(" ");
                sb.append(param.getSimpleName());
                first = false;
            }

            sb.append(")");
            sb.append(" : ").append(method.getReturnType().toString());
            return sb.toString();
        }
        return element.toString();
    }

    private List<CompletionItem> findImportCompletions(CompletionContext context) {
        List<CompletionItem> results = new ArrayList<>();

        String[] commonPackages = {
                "java.util", "java.io", "java.net", "java.lang",
                "javafx.scene", "javafx.stage", "javafx.application",
                "java.util.function", "java.util.stream"
        };

        for (String pkg : commonPackages) {
            if (pkg.startsWith(context.prefix)) {
                results.add(new CompletionItem(pkg, "package", ElementKind.PACKAGE, "Java package"));
            }
        }

        return results;
    }

    private List<CompletionItem> findLocalCompletions(javax.lang.model.util.Elements elements) {
        List<CompletionItem> results = new ArrayList<>();

        String[] keywords = {
                "abstract", "assert", "boolean", "break", "byte", "case",
                "catch", "char", "class", "const", "continue", "default",
                "do", "double", "else", "enum", "extends", "final",
                "finally", "float", "for", "if", "implements", "import",
                "instanceof", "int", "interface", "long", "native", "new",
                "package", "private", "protected", "public", "return",
                "short", "static", "strictfp", "super", "switch",
                "synchronized", "this", "throw", "throws", "transient",
                "try", "void", "volatile", "while", "true", "false", "null"
        };

        for (String keyword : keywords) {
            results.add(new CompletionItem(keyword, "keyword", ElementKind.OTHER, "Java keyword"));
        }

        String[] commonTypes = {
                "java.lang.String", "java.lang.Integer", "java.lang.Boolean",
                "java.util.List", "java.util.ArrayList", "java.util.Map",
                "java.util.HashMap", "java.io.File"
        };

        for (String typeName : commonTypes) {
            TypeElement type = elements.getTypeElement(typeName);
            if (type != null) {
                String simpleName = type.getSimpleName().toString();
                results.add(new CompletionItem(
                        simpleName,
                        "class",
                        ElementKind.CLASS,
                        typeName + " - Java class"
                ));
            }
        }

        for (String snippet : codeArea.codeSuggestionArray()) {
            results.add(new CompletionItem(snippet, "snippet", ElementKind.OTHER, "Code snippet"));
        }

        return results;
    }

    private static class CompletionContext {
        final String prefix;
        final String partial;
        final CompletionType type;
        final int startPosition;
        final int endPosition;

        CompletionContext(String prefix, String partial, CompletionType type,
                          int startPosition, int endPosition) {
            this.prefix = prefix;
            this.partial = partial;
            this.type = type;
            this.startPosition = startPosition;
            this.endPosition = endPosition;
        }
    }

    private enum CompletionType {
        SIMPLE_NAME,
        INSTANCE_MEMBER,
        STATIC_MEMBER,
        IMPORT
    }

    public static class CompletionItem {
        private final String name;
        private final String type;
        private final ElementKind kind;
        private final String detail;

        public CompletionItem(String name, String type, ElementKind kind, String detail) {
            this.name = name;
            this.type = type;
            this.kind = kind;
            this.detail = detail;
        }

        public String getName() { return name; }
        public String getType() { return type; }
        public ElementKind getKind() { return kind; }
        public String getDetail() { return detail; }

        @Override
        public String toString() {
            return name;
        }
    }
}