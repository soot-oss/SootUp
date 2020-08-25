/** @author: Hasitha Rajapakse */
package de.upb.swt.soot.jimple.parser.javatestsuite.java6;

import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.jimple.parser.categories.Java8Test;
import de.upb.swt.soot.jimple.parser.javatestsuite.JimpleTestSuiteBase;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(Java8Test.class)
public class LabelStatementTest extends JimpleTestSuiteBase {

  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "labelStatement", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  public List<String> expectedBodyStmts() {
    return Stream.of(
            "r0 := @this: LabelStatement",
            "$i0 = 20",
            "$i1 = 1",
            "label1:",
            "$z0 = $i1 < $i0",
            "if $z0 == 0 goto label3",
            "$i2 = $i1 % 10",
            "$z1 = $i2 == 0",
            "if $z1 == 0 goto label2",
            "goto label3",
            "label2:",
            "$i3 = $i1",
            "$i4 = $i1 + 1",
            "$i1 = $i4",
            "goto label1",
            "label3:",
            "return")
        .collect(Collectors.toList());
  }

  @Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
  }
}
