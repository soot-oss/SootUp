package de.upb.swt.soot.test.java.bytecode.minimaltestsuite.java6;

import categories.Java8Test;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.test.java.bytecode.minimaltestsuite.MinimalBytecodeTestSuiteBase;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;
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
            "l0 := @this: ForEachLoop",
            "$stack7 = newarray (int)[9]",
            "$stack7[0] = 10",
            "$stack7[1] = 20",
            "$stack7[2] = 30",
            "$stack7[3] = 40",
            "$stack7[4] = 50",
            "$stack7[5] = 60",
            "$stack7[6] = 71",
            "$stack7[7] = 80",
            "$stack7[8] = 90",
            "l1 = $stack7",
            "l2 = 0",
            "l3 = l1",
            "l4 = lengthof l3",
            "l5 = 0",
            "label1:",
            "$stack9 = l5",
            "$stack8 = l4",
            "if $stack9 >= $stack8 goto label2",
            "l6 = l3[l5]",
            "l2 = l2 + 1",
            "l5 = l5 + 1",
            "goto label1",
            "label2:",
            "return")
        .collect(Collectors.toList());
  }

  @Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
  }
}
