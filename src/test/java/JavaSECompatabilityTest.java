import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JavaSECompatabilityTest {

    @Test
    public void internalAPITest() throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(
                "jdeps", "--multi-release", "21", "--jdk-internals", "--class-path", "target/dependency/*"
        );

        Process process = pb.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        StringBuilder output = new StringBuilder();
        String line;
        boolean internalApiFound = false;

        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
            if (line.contains("JDK internal API") || line.contains("jdk.unsupported")) {
                internalApiFound = true;
            }
        }

        process.waitFor();

        assertFalse(internalApiFound, "JDK internal APIs detected:\n" + output);
    }

    @Test
    public void deprecatedAPITest() throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(
                "jdeprscan", "--release", "21", "--class-path", "target/classes;target/dependency/*", "target/classes"
        );

        Process process = pb.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        StringBuilder output = new StringBuilder();
        String line;
        boolean deprecatedApiFound = false;

        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
            if (line.contains("uses deprecated")) {
                deprecatedApiFound = true;
            }
        }

        process.waitFor();

        assertFalse(deprecatedApiFound, "Deprecated APIs detected:\n " + output);
    }

    @Test
    public void removedAPITest() throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(
                "jdeprscan", "--release", "21", "--for-removal", "--class-path", "target/classes;target/dependency/*", "target/classes"
        );

        Process process = pb.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        StringBuilder output = new StringBuilder();
        String line;
        boolean removedApiFound = false;

        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
            if (line.contains("uses removed")) {
                removedApiFound = true;
            }
        }

        process.waitFor();

        assertFalse(removedApiFound, "Removed APIs detected:\n " + output);
    }

}
