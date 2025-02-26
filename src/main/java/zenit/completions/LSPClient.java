package main.java.zenit.completions;

import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.services.LanguageClient;

import java.util.concurrent.CompletableFuture;


public class LSPClient implements LanguageClient {

    @Override
    public void telemetryEvent(Object o) {
        System.out.println("Telemetry event received: " + o);
    }

    @Override
    public void publishDiagnostics(PublishDiagnosticsParams publishDiagnosticsParams) {
        System.out.println("Diagnostics received: " + publishDiagnosticsParams.getDiagnostics());
    }

    @Override
    public void showMessage(MessageParams messageParams) {
        System.out.println(messageParams.getMessage());
    }

    @Override
    public CompletableFuture<MessageActionItem> showMessageRequest(ShowMessageRequestParams showMessageRequestParams) {
        return null;
    }

    @Override
    public void logMessage(MessageParams message) {
        System.out.println("[LSP Log] " + message.getMessage());
    }

}
