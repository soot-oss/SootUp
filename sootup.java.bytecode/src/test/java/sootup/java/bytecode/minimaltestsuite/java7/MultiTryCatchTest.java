package sootup.java.bytecode.minimaltestsuite.java7;

import categories.Java8Test;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.java.bytecode.minimaltestsuite.MinimalBytecodeTestSuiteBase;

/** @author Kaustubh Kelkar */
@Category(Java8Test.class)
public class MultiTryCatchTest extends MinimalBytecodeTestSuiteBase {

  @Test
  public void test() {
    SootMethod sootMethod = loadMethod(getMethodSignature());
    assertJimpleStmts(sootMethod, expectedBodyStmts());
  }

  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "printFile", "void", Collections.emptyList());
  }

  /**
   *
   *
   * <pre>
   * public void printFile() throws Exception {
   * BufferedReader bufferedReader = new BufferedReader(new FileReader("file.txt"));
   * try {
   * String data = "";
   * int divisor = 10/5;
   * System.out.println(divisor);
   * while ((data = bufferedReader.readLine()) != null) {
   * System.out.println(data);
   * }
   * }
   * catch( IOException | NumberFormatException e){
   *
   * }catch (Exception e){
   *
   * }finally {
   * try {
   * bufferedReader.close();
   * } catch (IOException e) {
   * }
   * }
   * }
   * }
   * </pre>
   */
  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "l0 := @this: MultiTryCatch",
            "$stack7 = new java.io.BufferedReader",
            "$stack6 = new java.io.FileReader",
            "specialinvoke $stack6.<java.io.FileReader: void <init>(java.lang.String)>(\"file.txt\")",
            "specialinvoke $stack7.<java.io.BufferedReader: void <init>(java.io.Reader)>($stack6)",
            "l1 = $stack7",
            "label01:",
            "l2 = \"\"",
            "l3 = 2",
            "$stack8 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $stack8.<java.io.PrintStream: void println(int)>(l3)",
            "label02:",
            "l2 = virtualinvoke l1.<java.io.BufferedReader: java.lang.String readLine()>()",
            "if l2 == null goto label16",
            "$stack9 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $stack9.<java.io.PrintStream: void println(java.lang.String)>(l2)",
            "goto label02",
            "label03:",
            "$stack15 := @caughtexception",
            "l2 = $stack15",
            "label04:",
            "virtualinvoke l1.<java.io.BufferedReader: void close()>()",
            "label05:",
            "goto label19",
            "label06:",
            "$stack14 := @caughtexception",
            "l2 = $stack14",
            "goto label19",
            "label07:",
            "$stack13 := @caughtexception",
            "l2 = $stack13",
            "label08:",
            "virtualinvoke l1.<java.io.BufferedReader: void close()>()",
            "label09:",
            "goto label19",
            "label10:",
            "$stack12 := @caughtexception",
            "l2 = $stack12",
            "goto label19",
            "label11:",
            "$stack11 := @caughtexception",
            "l4 = $stack11",
            "label12:",
            "virtualinvoke l1.<java.io.BufferedReader: void close()>()",
            "label13:",
            "goto label15",
            "label14:",
            "$stack10 := @caughtexception",
            "l5 = $stack10",
            "label15:",
            "throw l4",
            "label16:",
            "virtualinvoke l1.<java.io.BufferedReader: void close()>()",
            "label17:",
            "goto label19",
            "label18:",
            "$stack16 := @caughtexception",
            "l2 = $stack16",
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
