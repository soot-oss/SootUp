package sootup.java.bytecode;

import categories.TestCategories;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.jimple.basic.Trap;
import sootup.core.model.SourceType;
import sootup.core.signatures.MethodSignature;
import sootup.java.bytecode.inputlocation.PathBasedAnalysisInputLocation;
import sootup.java.core.views.JavaView;

@Tag(TestCategories.JAVA_8_CATEGORY)
public class TryCatchFinallyTests {

  @Test
  public void testTryWithResourcesFinally() {
    Path classFilePath =
        Paths.get("../shared-test-resources/bugfixes/TryWithResourcesFinally.class");

    AnalysisInputLocation inputLocation =
        new PathBasedAnalysisInputLocation.ClassFileBasedAnalysisInputLocation(
            classFilePath, "", SourceType.Application);
    JavaView view = new JavaView(Collections.singletonList(inputLocation));

    MethodSignature methodSignature =
        view.getIdentifierFactory()
            .parseMethodSignature("<TryWithResourcesFinally: void test0(java.lang.AutoCloseable)>");
    List<Trap> traps = view.getMethod(methodSignature).get().getBody().getTraps();
  }

  @Test
  public void testNestedTryCatchFinally() {
    Path classFilePath = Paths.get("../shared-test-resources/bugfixes/NestedTryCatchFinally.class");

    AnalysisInputLocation inputLocation =
        new PathBasedAnalysisInputLocation.ClassFileBasedAnalysisInputLocation(
            classFilePath, "", SourceType.Application);
    JavaView view = new JavaView(Collections.singletonList(inputLocation));

    MethodSignature methodSignature =
        view.getIdentifierFactory()
            .parseMethodSignature("<NestedTryCatchFinally: java.lang.String test0(java.io.File)>");
    List<Trap> traps = view.getMethod(methodSignature).get().getBody().getTraps();
  }
}
