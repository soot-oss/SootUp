package sootup.java.bytecode.inputlocation;

import categories.TestCategories;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.stream.Stream;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.model.SootMethod;
import sootup.java.core.JavaSootClass;
import sootup.java.core.views.JavaView;

@Tag(TestCategories.JAVA_8_CATEGORY)
public class FixJarsTest {

  String failed_jars_path = "../failed_jars";

  @Test
  public void executeFailedJars() {
    Path path = Paths.get(failed_jars_path);
    if (!Files.exists(path)) {
      System.out.println("Path does not exist: " + failed_jars_path);
      return;
    }
    // Walk through the directory and print file names
    try (Stream<Path> walk = Files.walk(path)) {
      walk.filter(Files::isRegularFile)
          .filter(file -> file.toString().endsWith(".jar"))
          .forEach(
              jar -> {
                createAnalysisInputLocation(jar.toString());
              });
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void createAnalysisInputLocation(String jarPath) {
    AnalysisInputLocation inputLocation = new JavaClassPathAnalysisInputLocation(jarPath);
    JavaView view = new JavaView(inputLocation);
    view.getClasses().forEach(JavaSootClass::getMethods);
  }

  private static void convertMethod() {
    AnalysisInputLocation inputLocation =
        new JavaClassPathAnalysisInputLocation("replace_with_the_jar_path");
    JavaView view = new JavaView(Collections.singletonList(inputLocation));
    final SootMethod sootMethod =
        view.getMethod(
                view.getIdentifierFactory()
                    .parseMethodSignature("replace_with_the_method_signature_you_want_to_jimplify"))
            .get();
    sootMethod.getBody();
  }

  @Disabled
  @Test
  public void testClass() {
    /* Example to start quickly */
    convertMethod();
  }
}
