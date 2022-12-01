package sootup.java.sourcecode.minimaltestsuite.java7;

import categories.Java8Test;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.java.sourcecode.minimaltestsuite.MinimalSourceTestSuiteBase;

@Category(Java8Test.class)
@Ignore("ms: trap building in wala is not working")
public class MultiTryCatchTest extends MinimalSourceTestSuiteBase {

  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "printFile", "void", Collections.emptyList());
  }

  @Override
  public List<String> expectedBodyStmts() {
    // ms: weird jimple: sequence of gotos - underlying wala instructions have the same anomaly!
    return Stream.of(
            "r0 := @this: MultiTryCatch",
            "label1:",
            "$r1 = new java.io.BufferedReader",
            "$r2 = new java.io.FileReader",
            "specialinvoke $r2.<java.io.FileReader: void <init>(java.lang.String)>(\"file.txt\")",
            "specialinvoke $r1.<java.io.BufferedReader: void <init>(java.io.Reader)>($r2)",
            "$r3 = \"\"",
            "$i0 = 10 / 5",
            "$r4 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $r4.<java.io.PrintStream: void println(int)>($i0)",
            "label2:",
            "$r5 = virtualinvoke $r1.<java.io.BufferedReader: java.lang.String readLine()>()",
            "$r3 = $r5",
            "$z0 = $r5 != null",
            "if $z0 == 0 goto label3",
            "$r6 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $r6.<java.io.PrintStream: void println(java.lang.String)>($r3)",
            "goto label2",
            "label3:",
            "goto label5",
            "label4:",
            "$r7 := @caughtexception",
            "$r8 = $r7",
            "label5:",
            "virtualinvoke $r1.<java.io.BufferedReader: void close()>()",
            "goto label7",
            "label6:",
            "$r9 := @caughtexception",
            "$r10 = $r9",
            "label7:",
            "return",
            "catch java.io.IOException from label1 to label4 with label4",
            "catch java.lang.NumberFormatException from label1 to label4 with label4",
            "catch java.io.IOException from label1 to label6 with label6")
        .collect(Collectors.toCollection(ArrayList::new));
  }

  @Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
  }
}
