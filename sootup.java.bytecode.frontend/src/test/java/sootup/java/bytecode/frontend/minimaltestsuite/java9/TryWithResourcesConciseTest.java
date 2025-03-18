package sootup.java.bytecode.frontend.minimaltestsuite.java9;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.java.bytecode.frontend.minimaltestsuite.MinimalBytecodeTestSuiteBase;

/**
 * @author Kaustubh Kelkar
 */
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
            "this := @this: TryWithResourcesConcise",
            "$stack6 = new java.io.BufferedReader",
            "$stack5 = new java.io.FileReader",
            "specialinvoke $stack5.<java.io.FileReader: void <init>(java.lang.String)>(\"file.txt\")",
            "specialinvoke $stack6.<java.io.BufferedReader: void <init>(java.io.Reader)>($stack5)",
            "l1 = $stack6",
            "l2 = l1",
            "label1:",
            "l3 = \"\"",
            "label2:",
            "l3 = virtualinvoke l1.<java.io.BufferedReader: java.lang.String readLine()>()",
            "if l3 == null goto label8",
            "$stack7 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $stack7.<java.io.PrintStream: void println(java.lang.String)>(l3)",
            "goto label2",
            "label3:",
            "$stack9 := @caughtexception",
            "l3 = $stack9",
            "if l2 == null goto label7",
            "label4:",
            "virtualinvoke l2.<java.io.BufferedReader: void close()>()",
            "label5:",
            "goto label7",
            "label6:",
            "$stack8 := @caughtexception",
            "l4 = $stack8",
            "virtualinvoke l3.<java.lang.Throwable: void addSuppressed(java.lang.Throwable)>(l4)",
            "label7:",
            "throw l3",
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
