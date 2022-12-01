package sootup.java.sourcecode.minimaltestsuite.java9;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Ignore;
import sootup.core.signatures.MethodSignature;
import sootup.java.sourcecode.minimaltestsuite.MinimalSourceTestSuiteBase;

/** @author Kaustubh Kelkar */
public class TryWithResourcesConciseTest extends MinimalSourceTestSuiteBase {

  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "printFile", "void", Collections.emptyList());
  }

  @Ignore
  public void test() {
    /** TODO [kk] Java 9 is not supported by WALA, feature can not be tested */
  }

  /**
   *
   *
   * <pre>
   *     public void printFile() throws Exception{
   * try(BufferedReader bufferedReader = new BufferedReader(new FileReader("file.txt"))){
   * String data = "";
   * while( (data= bufferedReader.readLine()) != null ){
   * System.out.println(data);
   * }
   * }
   * }
   * </pre>
   */
  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "r0 := @this: TryWithResourcesConcise",
            "$r1 = new java.io.BufferedReader",
            "$r2 = new java.io.FileReader",
            "specialinvoke $r2.<java.io.FileReader: void <init>(java.lang.String)>(\"file.txt\")",
            "specialinvoke $r1.<java.io.BufferedReader: void <init>(java.io.Reader)>($r2)",
            "$r3 = \"\"",
            "label1:",
            "$r4 = virtualinvoke $r1.<java.io.BufferedReader: java.lang.String readLine()>()",
            "goto label2",
            "$r5 := @caughtexception",
            "throw $r5",
            "label2:",
            "$r3 = $r4",
            "$z0 = $r4 != null",
            "if $z0 == 0 goto label3",
            "$r6 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $r6.<java.io.PrintStream: void println(java.lang.String)>($r3)",
            "goto label1",
            "label3:",
            "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }
}
