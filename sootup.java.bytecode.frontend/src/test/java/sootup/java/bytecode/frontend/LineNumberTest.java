package sootup.java.bytecode.frontend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.jimple.common.stmt.InvokableStmt;
import sootup.core.model.SourceType;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.JavaSootClass;
import sootup.java.core.JavaSootMethod;
import sootup.java.core.views.JavaView;

public class LineNumberTest {
  @Test
  public void test() {
    AnalysisInputLocation inputLocation =
        new JavaClassPathAnalysisInputLocation(
            "src/test/resources/linenumber/", SourceType.Application);

    JavaView view = new JavaView(inputLocation);
    JavaSootClass c2 =
        view.getClass(view.getIdentifierFactory().getClassType("b.c101")).orElse(null);
    assertNotNull(c2);
    JavaSootMethod m2 = c2.getMethod("m1177", Collections.emptyList()).orElse(null);
    assertNotNull(m2);
    int[] expected = new int[] {6716, 6718, 6720, 6722, 6724, 6726, 6728, 6733};
    List<Integer> invokeLineNumbers =
        m2.getBody().getStmts().stream()
            .filter(stmt -> stmt instanceof InvokableStmt)
            .filter(stmt -> ((InvokableStmt) stmt).getInvokeExpr().isPresent())
            .map(stmt -> stmt.getPositionInfo().getStmtPosition().getFirstLine())
            .toList();
    assertEquals(expected.length, invokeLineNumbers.size());

    for (int i = 0; i < expected.length; i++) {
      assertEquals(expected[i], invokeLineNumbers.get(i));
    }
  }
}
