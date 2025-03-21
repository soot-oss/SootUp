package sootup.java.bytecode.frontend;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.model.SourceType;
import sootup.core.signatures.MethodSignature;
import sootup.core.util.printer.BriefStmtPrinter;
import sootup.java.bytecode.frontend.inputlocation.ClassFileBasedAnalysisInputLocation;
import sootup.java.core.views.JavaView;

public class TryWithResourcesFinallyTests {

  Path classFilePath = Paths.get("../shared-test-resources/bugfixes/TryWithResourcesFinally.class");

  @Test
  public void test() {
    AnalysisInputLocation inputLocation =
        new ClassFileBasedAnalysisInputLocation(classFilePath, "", SourceType.Application);
    JavaView view = new JavaView(Collections.singletonList(inputLocation));

    MethodSignature methodSignature =
        view.getIdentifierFactory()
            .parseMethodSignature("<TryWithResourcesFinally: void test0(java.lang.AutoCloseable)>");
    BriefStmtPrinter stmtPrinter = new BriefStmtPrinter();
    stmtPrinter.buildTraps(view.getMethod(methodSignature).get().getBody().getStmtGraph());
    stmtPrinter.getTraps();
  }
}
