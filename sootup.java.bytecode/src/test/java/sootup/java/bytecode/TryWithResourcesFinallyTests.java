package sootup.java.bytecode;

import categories.TestCategories;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.jimple.basic.Trap;
import sootup.core.model.SourceType;
import sootup.core.signatures.MethodSignature;
import sootup.java.bytecode.inputlocation.DefaultRTJarAnalysisInputLocation;
import sootup.java.bytecode.inputlocation.PathBasedAnalysisInputLocation;
import sootup.java.core.views.JavaView;

@Tag(TestCategories.JAVA_8_CATEGORY)
public class TryWithResourcesFinallyTests {

  Path classFilePath = Paths.get("../shared-test-resources/bugfixes/TryWithResourcesFinally.class");

  @Test
  public void test() {
    AnalysisInputLocation inputLocation =
        new PathBasedAnalysisInputLocation.ClassFileBasedAnalysisInputLocation(
            classFilePath, "", SourceType.Application);
    JavaView view =
        new JavaView(Arrays.asList(new DefaultRTJarAnalysisInputLocation(), inputLocation));

    MethodSignature methodSignature =
        view.getIdentifierFactory()
            .parseMethodSignature("<TryWithResourcesFinally: void test0(java.lang.AutoCloseable)>");
    List<Trap> traps = view.getMethod(methodSignature).get().getBody().getTraps();
  }
}
