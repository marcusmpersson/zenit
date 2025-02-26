package main.java.zenit.completions;

import org.eclipse.lsp4j.services.LanguageServer;

import java.util.concurrent.CompletableFuture;

public class Test {
    public static void main(String[] args) throws Exception {

        String jdtlsLauncherPath = "lib/jdt-language-server-1.9.0-202203031534/plugins/org.eclipse.equinox.launcher_1.6.400.v20210924-0641.jar";
        String jdtlsConfigPath = "lib/jdt-language-server-1.9.0-202203031534/config_linux/";
        String jdtlsWorkspacePath = "/home/madara/Dokument/TestWorkspace";

        LanguageServer languageServer = LSPService.connect(jdtlsLauncherPath, jdtlsWorkspacePath, jdtlsConfigPath).get();

        CompletionModule completionModule = new CompletionModule(languageServer);

        String filePath = "";
        completionModule.openFile(filePath);
        completionModule.updateFile(filePath);
        CompletableFuture<Void> completions = completionModule.triggerCompletion(filePath, 1, 15);
        completions.get();
    }
}
