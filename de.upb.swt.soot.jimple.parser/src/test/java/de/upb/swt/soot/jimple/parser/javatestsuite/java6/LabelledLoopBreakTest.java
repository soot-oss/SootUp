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
public class LabelledLoopBreakTest extends JimpleTestSuiteBase {

  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "labelledLoopBreak", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  public List<String> expectedBodyStmts() {
    return Stream.of(
            "r0 := @this: LabelledLoopBreak",
            "$i0 = 0",
            "label1:",
            "$z0 = $i0 < 5",
            "if $z0 == 0 goto label5",
            "$i1 = 0",
            "label2:",
            "$z1 = $i1 < 5",
            "if $z1 == 0 goto label4",
            "$z2 = $i0 == 1",
            "if $z2 == 0 goto label3",
            "goto label5",
            "label3:",
            "$i2 = $i1",
            "$i3 = $i1 + 1",
            "$i1 = $i3",
            "goto label2",
            "label4:",
            "$i4 = $i0",
            "$i5 = $i0 + 1",
            "$i0 = $i5",
            "goto label1",
            "label5:",
            "return")
        .collect(Collectors.toList());
  }

  @Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
  }
}
