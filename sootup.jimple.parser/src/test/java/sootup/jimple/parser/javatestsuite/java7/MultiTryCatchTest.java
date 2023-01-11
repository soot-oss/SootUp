package sootup.jimple.parser.javatestsuite.java7;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.jimple.parser.categories.Java8Test;
import sootup.jimple.parser.javatestsuite.JimpleTestSuiteBase;

/** @author Kaustubh Kelkar */
@Category(Java8Test.class)
public class MultiTryCatchTest extends JimpleTestSuiteBase {

  @Test
  public void test() {
    SootMethod sootMethod = loadMethod(getMethodSignature());
    assertJimpleStmts(sootMethod, expectedBodyStmts());
  }

  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "printFile", "void", Collections.emptyList());
  }

  public List<String> expectedBodyStmts() {
    return Stream.of(
            "l0 := @this: MultiTryCatch",
            "$stack6 = new java.io.BufferedReader",
            "$stack7 = new java.io.FileReader",
            "specialinvoke $stack7.<java.io.FileReader: void <init>(java.lang.String)>(\"file.txt\")",
            "specialinvoke $stack6.<java.io.BufferedReader: void <init>(java.io.Reader)>($stack7)",
            "l1 = $stack6",
            "label01:",
            "l2 = \"\"",
            "l3 = 2",
            "$stack8 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $stack8.<java.io.PrintStream: void println(int)>(l3)",
            "label02:",
            "$stack11 = l1",
            "$stack9 = virtualinvoke $stack11.<java.io.BufferedReader: java.lang.String readLine()>()",
            "l2 = $stack9",
            "if $stack9 == null goto label16",
            "$stack10 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $stack10.<java.io.PrintStream: void println(java.lang.String)>(l2)",
            "goto label02",
            "label03:",
            "$stack18 := @caughtexception",
            "l2 = $stack18",
            "label04:",
            "virtualinvoke l1.<java.io.BufferedReader: void close()>()",
            "label05:",
            "goto label19",
            "label06:",
            "$stack17 := @caughtexception",
            "l2 = $stack17",
            "goto label19",
            "label07:",
            "$stack16 := @caughtexception",
            "l2 = $stack16",
            "label08:",
            "virtualinvoke l1.<java.io.BufferedReader: void close()>()",
            "label09:",
            "goto label19",
            "label10:",
            "$stack15 := @caughtexception",
            "l2 = $stack15",
            "goto label19",
            "label11:",
            "$stack13 := @caughtexception",
            "l4 = $stack13",
            "label12:",
            "virtualinvoke l1.<java.io.BufferedReader: void close()>()",
            "label13:",
            "goto label15",
            "label14:",
            "$stack12 := @caughtexception",
            "l5 = $stack12",
            "label15:",
            "$stack14 = l4",
            "throw $stack14",
            "label16:",
            "virtualinvoke l1.<java.io.BufferedReader: void close()>()",
            "label17:",
            "goto label19",
            "label18:",
            "$stack19 := @caughtexception",
            "l2 = $stack19",
            "goto label19",
            "label19:",
            "return",
            "catch java.io.IOException from label01 to label03 with label03",
            "catch java.lang.Exception from label01 to label03 with label07",
            "catch java.lang.NumberFormatException from label01 to label03 with label03",
            "catch java.lang.Throwable from label01 to label03 with label11",
            "catch java.io.IOException from label04 to label05 with label06",
            "catch java.io.IOException from label08 to label09 with label10",
            "catch java.lang.Throwable from label11 to label12 with label11",
            "catch java.io.IOException from label12 to label13 with label14",
            "catch java.io.IOException from label16 to label17 with label18")
        .collect(Collectors.toCollection(ArrayList::new));
  }
}
