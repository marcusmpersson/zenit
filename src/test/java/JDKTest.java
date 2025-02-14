import main.java.zenit.filesystem.jreversions.JREVersions;
import main.java.zenit.setup.SetupController;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;


public class JDKTest {

    @Test
    void JDKVersion() {

        SetupController sc = new SetupController();

        List<String> defaultJdk = sc.getJDKs();
        String defaultJDK = sc.getDefaultJDK();

        assert defaultJdk.contains(defaultJDK + " [default]");
    }

    @Test
    void JDKFileVerification() {

        JREVersions jreVersions = new JREVersions();

        File JDKDir = jreVersions.getJVMDirectory();
        assert JDKDir != null && JDKDir.exists();

        for(File JDK : JDKDir.listFiles()) {
            File[] files = JDK.listFiles();
            assert files != null;
            for(File file : files) {
                if(file.getName().equals("bin")) {
                    File[] binFiles = file.listFiles();
                    for(File binFile : binFiles) {
                        String name = binFile.getName();

                        if(name.equals("java") || name.equals("javac") || name.equals("javadoc") || name.equals("jar")) {
                            assert binFile.exists();
                        }
                    }
                }
            }
        }
    }
}
