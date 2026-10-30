package sootup.java.bytecode.frontend;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.model.Body;
import sootup.core.model.SourceType;
import sootup.core.signatures.MethodSignature;
import sootup.core.util.printer.BriefStmtPrinter;
import sootup.interceptors.LocalSplitter;
import sootup.interceptors.TypeAssigner;
import sootup.java.bytecode.frontend.inputlocation.ClassFileBasedAnalysisInputLocation;
import sootup.java.core.views.JavaView;

/** block iteration (i.e. serialization + trap building) of nested exceptional flows */
public class TryCatchFinallyTests {

  @Test
  public void testNestedTryCatchFinally() {
    Path classFilePath = Paths.get("src/test/resources/bugfixes/NestedTryCatchFinally.class");

    AnalysisInputLocation inputLocation =
        new ClassFileBasedAnalysisInputLocation(classFilePath, "", SourceType.Application);
    JavaView view = new JavaView(Collections.singletonList(inputLocation));

    MethodSignature methodSignature =
        view.getIdentifierFactory()
            .parseMethodSignature("<NestedTryCatchFinally: java.lang.String test0(java.io.File)>");
    Body body = view.getMethod(methodSignature).get().getBody();
    BriefStmtPrinter stmtPrinter = new BriefStmtPrinter();
    stmtPrinter.buildTraps(body.getControlFlowGraph());
    assertFalse(stmtPrinter.getTraps().isEmpty());
    assertFalse(body.toString().isEmpty());
  }

  @Test
  public void testTryCatchFinallyIterator() {
    Path classFilePath = Paths.get("src/test/resources/bugfixes/LineIterator.class");

    AnalysisInputLocation inputLocation =
        new ClassFileBasedAnalysisInputLocation(
            classFilePath,
            "",
            SourceType.Application,
            Arrays.asList(new LocalSplitter(), new TypeAssigner()));
    JavaView view = new JavaView(Collections.singletonList(inputLocation));
    view.getClasses()
        .forEach(
            clazz ->
                clazz.getMethods().stream()
                    .filter(method -> method.hasBody())
                    .forEach(
                        method -> {
                          Body body = view.getMethod(method.getSignature()).get().getBody();
                          new BriefStmtPrinter().buildTraps(body.getControlFlowGraph());
                          assertFalse(body.toString().isEmpty());
                        }));
  }
}
