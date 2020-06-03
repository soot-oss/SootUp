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
public class TryCatchFinallyTest extends MinimalBytecodeTestSuiteBase {
  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "tryCatchFinally", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  @Test
  public void test() {
    SootMethod sootMethod = loadMethod(getMethodSignature());
    assertJimpleStmts(sootMethod, expectedBodyStmts());
  }

  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "l0 := @this: TryCatchFinally",
            "l1 = \"\"",
            "label1:",
            "l1 = \"this is try block\"",
            "l2 = 0",
            "l2 = l2 + 1",
            "$stack4 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $stack4.<java.io.PrintStream: void println(int)>(l2)",
            "label2:",
            "l1 = \"this is finally block\"",
            "goto label6",
            "label3:",
            "$stack6 := @caughtexception",
            "l2 = $stack6",
            "l1 = \"this is catch block\"",
            "label4:",
            "l1 = \"this is finally block\"",
            "goto label6",
            "label5:",
            "$stack5 := @caughtexception",
            "l3 = $stack5",
            "l1 = \"this is finally block\"",
            "throw l3",
            "label6:",
            "return",
            "catch java.lang.Exception from label1 to label2 with label3",
            "catch java.lang.Throwable from label1 to label2 with label5",
            "catch java.lang.Throwable from label3 to label4 with label5")
        .collect(Collectors.toList());
  }
}
