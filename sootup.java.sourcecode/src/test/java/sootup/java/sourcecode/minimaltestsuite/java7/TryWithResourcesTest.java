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

/** @author Kaustubh Kelkar */
@Category(Java8Test.class)
@Ignore("FIXME: ms: wala does not convert traps correctly.")
public class TryWithResourcesTest extends MinimalSourceTestSuiteBase {

  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "printFile", "void", Collections.emptyList());
  }

  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "r0 := @this: TryWithResources",
            "label1:",
            "$r1 = new java.io.BufferedReader",
            "$r2 = new java.io.FileReader",
            "specialinvoke $r2.<java.io.FileReader: void <init>(java.lang.String)>(\"file.txt\")",
            "specialinvoke $r1.<java.io.BufferedReader: void <init>(java.io.Reader)>($r2)",
            "$r3 = \"\"",
            "label2:",
            "$r4 = virtualinvoke $r1.<java.io.BufferedReader: java.lang.String readLine()>()",
            "goto label4",
            "label3:",
            "$r5 := @caughtexception",
            "virtualinvoke $r1.<java.io.BufferedReader: void close()>()",
            "throw $r5",
            "label4:",
            "$r3 = $r4",
            "$z0 = $r4 != null",
            "if $z0 == 0 goto label5",
            "$r6 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $r6.<java.io.PrintStream: void println(java.lang.String)>($r3)",
            "goto label2",
            "label5:",
            "virtualinvoke $r1.<java.io.BufferedReader: void close()>()",
            "return",
            "catch java.lang.Throwable from label1 to label3 with label3")
        .collect(Collectors.toCollection(ArrayList::new));
  }

  @Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
  }
}
