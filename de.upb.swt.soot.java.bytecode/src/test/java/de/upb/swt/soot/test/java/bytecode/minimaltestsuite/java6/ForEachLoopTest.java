package de.upb.swt.soot.test.java.bytecode.minimaltestsuite.java6;

import categories.Java8Test;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.test.java.bytecode.minimaltestsuite.MinimalBytecodeTestSuiteBase;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.experimental.categories.Category;

/** @author Kaustubh Kelkar */
@Category(Java8Test.class)
public class ForEachLoopTest extends MinimalBytecodeTestSuiteBase {

  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "forEachLoop", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "r0 := @this: ForEachLoop",
            "$r1 = newarray (int[])[9]",
            "$r1[0] = 10",
            "$r1[1] = 20",
            "$r1[2] = 30",
            "$r1[3] = 40",
            "$r1[4] = 50",
            "$r1[5] = 60",
            "$r1[6] = 71",
            "$r1[7] = 80",
            "$r1[8] = 90",
            "$i0 = 0",
            "$r2 = $r1",
            "$i1 = 0",
            "label1:",
            "$i2 = lengthof $r2",
            "$z0 = $i1 < $i2",
            "if $z0 == 0 goto label2",
            "$r3 = $r2[$i1]",
            "$i3 = $i0",
            "$i4 = $i0 + 1",
            "$i0 = $i4",
            "$i5 = $i1",
            "$i6 = $i1 + 1",
            "$i1 = $i6",
            "goto label1",
            "label2:",
            "return")
        .collect(Collectors.toList());
  }
}
