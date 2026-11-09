package sootup.java.bytecode.frontend.minimaltestsuite.java10;

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
public class TypeInferenceTest extends MinimalBytecodeTestSuiteBase {

  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "printFile", "void", Collections.emptyList());
  }

  /**
   *
   *
   * <pre>
   * public void printFile() throws Exception{
   * var fileName="file.txt";
   * var data = "";
   * var fileReader= new FileReader(fileName);
   * var bufferedReader= new BufferedReader(fileReader);
   * while( (data= bufferedReader.readLine()) != null ){
   * System.out.println(data);
   * }
   * bufferedReader.close();
   * }
   *
   * public static void main(String[] args) throws Exception{
   * TypeInference typeInference = new TypeInference();
   * typeInference.printFile();
   * }
   * }
   * </pre>
   */
  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "this := @this: TypeInference",
            "l1 = \"file.txt\"",
            "l2 = \"\"",
            "$stack5 = new java.io.FileReader",
            "specialinvoke $stack5.<java.io.FileReader: void <init>(java.lang.String)>(l1)",
            "l3 = $stack5",
            "$stack6 = new java.io.BufferedReader",
            "specialinvoke $stack6.<java.io.BufferedReader: void <init>(java.io.Reader)>(l3)",
            "l4 = $stack6",
            "label1:",
            "l2 = virtualinvoke l4.<java.io.BufferedReader: java.lang.String readLine()>()",
            "if l2 == null goto label2",
            "$stack7 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $stack7.<java.io.PrintStream: void println(java.lang.String)>(l2)",
            "goto label1",
            "label2:",
            "virtualinvoke l4.<java.io.BufferedReader: void close()>()",
            "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }

  @Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
  }
}
