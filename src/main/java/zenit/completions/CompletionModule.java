package main.java.zenit.completions;

import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class CompletionModule {
    private final LanguageServer server;
    private final Map<String, Integer> fileVersions = new ConcurrentHashMap<>();

    public CompletionModule(LanguageServer server) {
        this.server = server;
    }

    public void openFile(String filePath) throws Exception {
        Path path = Paths.get(filePath);
        String uri = path.toUri().toString();
        String content = Files.readString(path);

        fileVersions.put(uri, 1);

        TextDocumentItem document = new TextDocumentItem(uri, "java", 1, content);
        DidOpenTextDocumentParams params = new DidOpenTextDocumentParams(document);

        server.getTextDocumentService().didOpen(params);
    }

    public void updateFile(String filePath) throws Exception {
        Path path = Paths.get(filePath);
        String uri = path.toUri().toString();
        String newContent = Files.readString(path);

        fileVersions.put(uri, fileVersions.get(uri) + 1);

        TextDocumentContentChangeEvent change = new TextDocumentContentChangeEvent(newContent);
        DidChangeTextDocumentParams changeParams = new DidChangeTextDocumentParams();

        changeParams.setTextDocument(new VersionedTextDocumentIdentifier(uri, fileVersions.get(uri)));
        changeParams.setContentChanges(List.of(change));

        server.getTextDocumentService().didChange(changeParams);
    }

    public CompletableFuture<Void> triggerCompletion(String filePath, int position, int character) {
        System.out.println("Triggering completion for " + filePath + " at line " + position + ", character " + character + "...");

        Path path = Paths.get(filePath);
        String uri = path.toUri().toString();

        TextDocumentIdentifier docId = new TextDocumentIdentifier(uri);
        Position pos = new Position(position, character);
        CompletionParams params = new CompletionParams(docId, pos);


        return server.getTextDocumentService().completion(params)
                .thenAccept(completionResult -> {
                    System.out.println("Completion Items for " + uri + " at line " + position + ", character " + character + ":");
                    if (completionResult.isLeft()) {
                        CompletionList list = (CompletionList) completionResult.getLeft();
                        for (CompletionItem item : list.getItems()) {
                            System.out.println(" - " + item.getLabel());
                        }
                    } else {
                        List<CompletionItem> items = (List<CompletionItem>) completionResult.getRight();
                        for (CompletionItem item : items) {
                            System.out.println(" - " + item.getLabel());
                        }
                    }
                });
    }
}
