import main.java.zenit.setup.SetupController;
import org.junit.jupiter.api.Test;
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

    }
}
