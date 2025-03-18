package sootup.jimple.frontend.javatestsuite.java7;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.jimple.frontend.javatestsuite.JimpleTestSuiteBase;

/**
 * @author Kaustubh Kelkar
 */
public class TryWithResourcesTest extends JimpleTestSuiteBase {

  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "printFile", "void", Collections.emptyList());
  }

  @Test
  public void test() {
    SootMethod sootMethod = loadMethod(getMethodSignature());
    assertJimpleStmts(sootMethod, expectedBodyStmts());
  }

  public List<String> expectedBodyStmts() {
    return Stream.of(
            "l0 := @this: TryWithResources",
            "$stack6 = new java.io.BufferedReader",
            "$stack7 = new java.io.FileReader",
            "specialinvoke $stack7.<java.io.FileReader: void <init>(java.lang.String)>(\"file.txt\")",
            "specialinvoke $stack6.<java.io.BufferedReader: void <init>(java.io.Reader)>($stack7)",
            "l1 = $stack6",
            "l2 = null",
            "label01:",
            "l3 = \"\"",
            "label02:",
            "$stack10 = l1",
            "$stack8 = virtualinvoke $stack10.<java.io.BufferedReader: java.lang.String readLine()>()",
            "l3 = $stack8",
            "if $stack8 == null goto label09",
            "$stack9 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $stack9.<java.io.PrintStream: void println(java.lang.String)>(l3)",
            "goto label02",
            "label03:",
            "$stack14 := @caughtexception",
            "l3 = $stack14",
            "l2 = l3",
            "throw l3",
            "label04:",
            "$stack12 := @caughtexception",
            "l4 = $stack12",
            "label05:",
            "if l1 == null goto label15",
            "if l2 == null goto label14",
            "label06:",
            "virtualinvoke l1.<java.io.BufferedReader: void close()>()",
            "label07:",
            "goto label15",
            "label08:",
            "$stack11 := @caughtexception",
            "l5 = $stack11",
            "virtualinvoke l2.<java.lang.Throwable: void addSuppressed(java.lang.Throwable)>(l5)",
            "goto label15",
            "label09:",
            "if l1 == null goto label16",
            "if l2 == null goto label13",
            "label10:",
            "virtualinvoke l1.<java.io.BufferedReader: void close()>()",
            "label11:",
            "goto label16",
            "label12:",
            "$stack15 := @caughtexception",
            "l3 = $stack15",
            "virtualinvoke l2.<java.lang.Throwable: void addSuppressed(java.lang.Throwable)>(l3)",
            "goto label16",
            "label13:",
            "virtualinvoke l1.<java.io.BufferedReader: void close()>()",
            "goto label16",
            "label14:",
            "virtualinvoke l1.<java.io.BufferedReader: void close()>()",
            "label15:",
            "$stack13 = l4",
            "throw $stack13",
            "label16:",
            "return",
            "catch java.lang.Throwable from label01 to label03 with label03",
            "catch java.lang.Throwable from label03 to label05 with label04",
            "catch java.lang.Throwable from label06 to label07 with label08",
            "catch java.lang.Throwable from label10 to label11 with label12")
        .collect(Collectors.toCollection(ArrayList::new));
  }
}
