package sootup.jimple.parser.javatestsuite.java10;

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
public class TypeInferenceTest extends JimpleTestSuiteBase {

  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "printFile", "void", Collections.emptyList());
  }

  public List<String> expectedBodyStmts() {
    return Stream.of(
            "l0 := @this: TypeInference",
            "l1 = \"file.txt\"",
            "l2 = \"\"",
            "$stack5 = new java.io.FileReader",
            "specialinvoke $stack5.<java.io.FileReader: void <init>(java.lang.String)>(l1)",
            "l3 = $stack5",
            "$stack6 = new java.io.BufferedReader",
            "specialinvoke $stack6.<java.io.BufferedReader: void <init>(java.io.Reader)>(l3)",
            "l4 = $stack6",
            "label1:",
            "$stack9 = l4",
            "$stack7 = virtualinvoke $stack9.<java.io.BufferedReader: java.lang.String readLine()>()",
            "l2 = $stack7",
            "if $stack7 == null goto label2",
            "$stack8 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $stack8.<java.io.PrintStream: void println(java.lang.String)>(l2)",
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
