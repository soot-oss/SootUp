package sootup.java.bytecode.minimaltestsuite.java7;

import categories.TestCategories;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.java.bytecode.minimaltestsuite.MinimalBytecodeTestSuiteBase;

/** @author Kaustubh Kelkar */
@Tag(TestCategories.JAVA_8_CATEGORY)
public class TryWithResourcesTest extends MinimalBytecodeTestSuiteBase {

  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "printFile", "void", Collections.emptyList());
  }

  @Test
  public void test() {
    SootMethod sootMethod = loadMethod(getMethodSignature());
    assertJimpleStmts(sootMethod, expectedBodyStmts());
  }

  /**
   *
   *
   * <pre>
   * public void printFile() throws Exception{
   * try(BufferedReader bufferedReader = new BufferedReader(new FileReader("file.txt"))){
   * String data = "";
   * while( (data= bufferedReader.readLine()) != null ){
   * System.out.println(data);
   * }
   * }
   * }
   * }
   * </pre>
   */
  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "this := @this: TryWithResources",
            "$stack7 = new java.io.BufferedReader",
            "$stack6 = new java.io.FileReader",
            "specialinvoke $stack6.<java.io.FileReader: void <init>(java.lang.String)>(\"file.txt\")",
            "specialinvoke $stack7.<java.io.BufferedReader: void <init>(java.io.Reader)>($stack6)",
            "l1 = $stack7",
            "l2 = null",
            "label01:",
            "l3 = \"\"",
            "label02:",
            "l3 = virtualinvoke l1.<java.io.BufferedReader: java.lang.String readLine()>()",
            "if l3 == null goto label11",
            "$stack8 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $stack8.<java.io.PrintStream: void println(java.lang.String)>(l3)",
            "goto label02",
            "label03:",
            "$stack11 := @caughtexception",
            "l3 = $stack11",
            "l2 = l3",
            "throw l3",
            "label04:",
            "$stack10 := @caughtexception",
            "l4 = $stack10",
            "label05:",
            "if l1 == null goto label10",
            "if l2 == null goto label09",
            "label06:",
            "virtualinvoke l1.<java.io.BufferedReader: void close()>()",
            "label07:",
            "goto label10",
            "label08:",
            "$stack9 := @caughtexception",
            "l5 = $stack9",
            "virtualinvoke l2.<java.lang.Throwable: void addSuppressed(java.lang.Throwable)>(l5)",
            "goto label10",
            "label09:",
            "virtualinvoke l1.<java.io.BufferedReader: void close()>()",
            "label10:",
            "throw l4",
            "label11:",
            "if l1 == null goto label16",
            "if l2 == null goto label15",
            "label12:",
            "virtualinvoke l1.<java.io.BufferedReader: void close()>()",
            "label13:",
            "goto label16",
            "label14:",
            "$stack12 := @caughtexception",
            "l3 = $stack12",
            "virtualinvoke l2.<java.lang.Throwable: void addSuppressed(java.lang.Throwable)>(l3)",
            "goto label16",
            "label15:",
            "virtualinvoke l1.<java.io.BufferedReader: void close()>()",
            "goto label16",
            "label16:",
            "return",
            "catch java.lang.Throwable from label01 to label03 with label03",
            "catch java.lang.Throwable from label03 to label05 with label04",
            "catch java.lang.Throwable from label06 to label07 with label08",
            "catch java.lang.Throwable from label12 to label13 with label14")
        .collect(Collectors.toCollection(ArrayList::new));
  }
}
