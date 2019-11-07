/** @author: Hasitha Rajapakse */
package de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.java6;

import categories.Java8Test;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.MinimalTestSuiteBase;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.experimental.categories.Category;

@Category(Java8Test.class)
public class DeclareEnumWithConstructorTest extends MinimalTestSuiteBase {

  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "declareEnum", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "r0 := @this: DeclareEnumWithConstructor",
            "$r1 = staticinvoke <DeclareEnum$Type: DeclareEnum$Type[] values()>()",
            "$i0 = 0",
            "label1:",
            "$i1 = lengthof $r1",
            "$z0 = $i0 < $i1",
            "if $z0 == 0 goto label2",
            "$r2 = $r1[$i0]",
            "$r3 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $r3.<java.io.PrintStream: void println(java.lang.Object)>($r2)",
            "$i2 = $i0",
            "$i3 = $i0 + 1",
            "$i0 = $i3",
            "goto label1",
            "label2:",
            "return")
        .collect(Collectors.toList());
  }
}
