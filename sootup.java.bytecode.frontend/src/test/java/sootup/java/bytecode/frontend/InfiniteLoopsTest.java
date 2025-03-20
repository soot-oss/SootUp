package sootup.java.bytecode.frontend;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.model.Body;
import sootup.core.model.SourceType;
import sootup.core.signatures.MethodSignature;
import sootup.java.bytecode.frontend.inputlocation.ClassFileBasedAnalysisInputLocation;
import sootup.java.core.views.JavaView;

public class InfiniteLoopsTest {

  Path classFilePath = Paths.get("../shared-test-resources/bugfixes/InfiniteLoops.class");

  @Test
  public void test() {
    AnalysisInputLocation inputLocation =
        new ClassFileBasedAnalysisInputLocation(
            classFilePath, "", SourceType.Application, Collections.emptyList());
    JavaView view = new JavaView(Collections.singletonList(inputLocation));

    final MethodSignature methodSignature =
        view.getIdentifierFactory()
            .getMethodSignature("InfiniteLoops", "tc1", "void", Collections.emptyList());
    Body body = view.getMethod(methodSignature).get().getBody();
    assertFalse(body.getStmts().isEmpty());
    assertDoesNotThrow(() -> view.getMethod(methodSignature).get().getBody());

    final MethodSignature methodSignature2 =
        view.getIdentifierFactory()
            .getMethodSignature("InfiniteLoops", "tc2", "void", Collections.emptyList());
    Body body2 = view.getMethod(methodSignature2).get().getBody();
    assertFalse(body2.getStmts().isEmpty());
    assertDoesNotThrow(() -> view.getMethod(methodSignature2).get().getBody());

    final MethodSignature methodSignature3 =
        view.getIdentifierFactory()
            .getMethodSignature("InfiniteLoops", "tc3", "void", Collections.emptyList());
    Body body3 = view.getMethod(methodSignature3).get().getBody();
    assertFalse(body3.getStmts().isEmpty());
    assertDoesNotThrow(() -> view.getMethod(methodSignature3).get().getBody());
  }
}
