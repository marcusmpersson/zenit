package main.java.zenit.completions;

import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;

import javax.print.Doc;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;

public class LSPService {

    public static CompletableFuture<LanguageServer> connect(String jdtlsLauncherJarPath, String workspacePath, String configurationPath) throws Exception {

        Process jdtlsProcess = getProcess(jdtlsLauncherJarPath, workspacePath, configurationPath);

        new Thread(() -> {
            try (Scanner scanner = new Scanner(jdtlsProcess.getInputStream())) {
                while (scanner.hasNextLine()) {
                    System.out.println("[JDT LS] " + scanner.nextLine());
                }
            }
        }).start();

        InputStream processInput = jdtlsProcess.getInputStream();
        OutputStream processOutput = jdtlsProcess.getOutputStream();

        LSPClient client = new LSPClient();

        Launcher<LanguageServer> launcher = Launcher.createLauncher(client, LanguageServer.class, processInput, processOutput);
        LanguageServer server = launcher.getRemoteProxy();

        launcher.startListening();

        InitializeParams initParams = new InitializeParams();
        CompletableFuture<InitializeResult> initResult = server.initialize(initParams);
        return initResult.thenApply(result -> {
            System.out.println("JDT LS initialized with capabilities: " + result.getCapabilities());
            return server;
        });
    }

    private static Process getProcess(String jdtlsLauncherJarPath, String workspacePath, String configurationPath) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder(
                "java",
                "-Declipse.application=org.eclipse.jdt.ls.core.id1",
                "-Dosgi.bundles.defaultStartLevel=4",
                "-Declipse.product=org.eclipse.jdt.ls.core.product",
                "-jar",
                jdtlsLauncherJarPath,
                "-configuration",
                configurationPath,
                "-data",
                workspacePath
        );
        processBuilder.redirectErrorStream(true);
        return processBuilder.start();
    }
}
