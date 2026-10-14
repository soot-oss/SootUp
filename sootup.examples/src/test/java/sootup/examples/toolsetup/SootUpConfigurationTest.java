package sootup.examples.toolsetup;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Paths;
import org.junit.jupiter.api.Test;
import sootup.java.core.views.JavaView;

public class SootUpConfigurationTest {

  @Test
  public void classpathAndMainClass() throws Exception {
    // --8<-- [start:test]
    String cp = Paths.get("src/test/resources/Liveness/binary").toAbsolutePath().toString();

    // Mimics: java -cp <path> Example
    SootUpConfiguration config = new SootUpConfiguration("--cp", cp, "Example");

    JavaView view = config.getView();
    assertNotNull(view);
    // The entry point is resolved into a valid MethodSignature.
    assertEquals("Example", config.getEntrypoint().getDeclClassType().getClassName());
    // --8<-- [end:test]
  }

  @Test
  public void allThreeClasspathAliasesAreAccepted() throws Exception {
    String cp = Paths.get("src/test/resources/Liveness/binary").toAbsolutePath().toString();

    // java accepts --classpath, --class-path, and --cp interchangeably.
    new SootUpConfiguration("--classpath", cp, "Example");
    new SootUpConfiguration("--class-path", cp, "Example");
    new SootUpConfiguration("--cp", cp, "Example");
  }
}
