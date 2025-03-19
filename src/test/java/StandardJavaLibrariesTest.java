import main.java.zenit.javacodecompiler.JavaSourceCodeCompiler;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.*;

public class StandardJavaLibrariesTest {

    private static final String CLASS_NAME = "TestStandardLibs";
    private static final String JAVA_FILE_NAME = CLASS_NAME + ".java";


    @Test
    void testUtilLibrary() throws Exception {
        String javaCode = "import java.util.ArrayList;\n" +
                "                public class TestStandardLibs {\n" +
                "                    public static void main(String[] args) {\n" +
                "                        ArrayList<String> list = new ArrayList<>();\n" +
                "                        list.add(\"ArrayList Success\");\n" +
                "                        System.out.println(list.get(0));\n" +
                "                    }\n" +
                "                }";
        executeTest(javaCode, "ArrayList Success");
    }

    @Test
    void testIoLibrary() throws Exception {
        String javaCode = "import java.io.*;\n" +
                "               public class TestStandardLibs {\n" +
                "                   public static void main(String[] args) throws Exception {\n" +
                "                       File file = new File(\"testfile.txt\");\n" +
                "                       try (FileWriter writer = new FileWriter(file)) {\n " +
                "                           writer.write(\"File I/O Success\");}\n" +
                "                       try (BufferedReader reader = new BufferedReader(new FileReader(file))) { " +
                "                           System.out.println(reader.readLine());}\n" +
                "                       file.delete();\n" +
                "                   }\n" +
                "               }";
        executeTest(javaCode, "File I/O Success");
    }

    @Test
    void testNioLibrary() throws Exception {
        String javaCode = "import java.nio.file.*;\n" +
                "          import java.io.IOException;\n" +
                "               public class TestStandardLibs {\n " +
                "                   public static void main(String[] args) throws IOException {\n " +
                "                       Path path = Paths.get(\"testfile.txt\");\n " +
                "                       Files.writeString(path, \"NIO Success\");\n" +
                "                       System.out.println(Files.readString(path));\n " +
                "                       Files.delete(path);\n" +
                "                   }\n" +
                "               }";
        executeTest(javaCode, "NIO Success");
    }

    private void executeTest(String javaCode, String expectedOutput) throws Exception {
        //create a temporary directory for compilation
        Path tempDir = Files.createTempDirectory("zenit-test");
        Path javaFilePath = tempDir.resolve(JAVA_FILE_NAME);

        //write Java code to file
        Files.writeString(javaFilePath, javaCode);

        //redirect System.out to capture output
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        PrintStream newOut = new PrintStream(outputStream);
        System.setOut(newOut);

        try {
            //compile and run using Zenit
            JavaSourceCodeCompiler compiler = new JavaSourceCodeCompiler(javaFilePath.toFile(), false);

            compiler.startCompileAndRun();

            //wait for execution to complete
            Thread.sleep(3000);

            //print JDK path - should be the default JDK path
            System.out.println("Using JDK: " + compiler.getJDKPath());

            //get the output
            String output = outputStream.toString().trim();

            //print output
            System.setOut(originalOut);
            System.out.println("Captured Output: " + output);

            //assert expected output
            assertTrue(output.contains(expectedOutput), "Unexpected output from program!");
        } finally {
            //restore original System.out
            System.setOut(originalOut);

            //cleanup
            Files.deleteIfExists(javaFilePath);
            Files.walk(tempDir).sorted(Comparator.reverseOrder()).forEach(path -> {
                try { Files.delete(path); } catch (Exception ignored) {}
            });
        }
    }
}
