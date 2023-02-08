package sootup.java.bytecode.minimaltestsuite.java9;

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
public class TryWithResourcesConciseTest extends MinimalBytecodeTestSuiteBase {

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
  /**
   *
   *
   * <pre>
   * public void printFile() throws Exception {
   * final BufferedReader bufferedReader = new BufferedReader(new FileReader("file.txt"));
   * try(bufferedReader) {
   * String data = "";
   * while( (data= bufferedReader.readLine()) != null) {
   * System.out.println(data);
   * }
   * }
   * }
   *
   * public static void main(String[] args) throws  Exception{
   * TryWithResourcesConcise tryWithResourcesConcise = new TryWithResourcesConcise();
   * tryWithResourcesConcise.printFile();
   * }
   * }
   * </pre>
   */
  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "l0 := @this: TryWithResourcesConcise",
            "$stack5 = new java.io.BufferedReader",
            "$stack6 = new java.io.FileReader",
            "specialinvoke $stack6.<java.io.FileReader: void <init>(java.lang.String)>(\"file.txt\")",
            "specialinvoke $stack5.<java.io.BufferedReader: void <init>(java.io.Reader)>($stack6)",
            "l1 = $stack5",
            "l2 = l1",
            "label1:",
            "l3 = \"\"",
            "label2:",
            "$stack9 = l1",
            "$stack7 = virtualinvoke $stack9.<java.io.BufferedReader: java.lang.String readLine()>()",
            "l3 = $stack7",
            "if $stack7 == null goto label8",
            "$stack8 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $stack8.<java.io.PrintStream: void println(java.lang.String)>(l3)",
            "goto label2",
            "label3:",
            "$stack11 := @caughtexception",
            "l3 = $stack11",
            "if l2 == null goto label7",
            "label4:",
            "virtualinvoke l2.<java.io.BufferedReader: void close()>()",
            "label5:",
            "goto label7",
            "label6:",
            "$stack10 := @caughtexception",
            "l4 = $stack10",
            "virtualinvoke l3.<java.lang.Throwable: void addSuppressed(java.lang.Throwable)>(l4)",
            "label7:",
            "$stack12 = l3",
            "throw $stack12",
            "label8:",
            "if l2 == null goto label9",
            "virtualinvoke l2.<java.io.BufferedReader: void close()>()",
            "goto label9",
            "label9:",
            "return",
            "catch java.lang.Throwable from label1 to label3 with label3",
            "catch java.lang.Throwable from label4 to label5 with label6")
        .collect(Collectors.toCollection(ArrayList::new));
  }
}
