package sootup.java.bytecode.frontend.minimaltestsuite.java6;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.java.bytecode.frontend.minimaltestsuite.MinimalBytecodeTestSuiteBase;

/**
 * @author Kaustubh Kelkar
 */
public class DeclareEnumTest extends MinimalBytecodeTestSuiteBase {

  @Test
  public void test() {
    SootClass sc =
        loadClass(
            identifierFactory.getClassType(
                getDeclaredClassSignature().getFullyQualifiedName() + "$Type"));
    assertTrue(sc.isEnum());
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
  }

  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "declareEnum", "void", Collections.emptyList());
  }

  /**  <pre>
   * public void declareEnum(){
   * for(Type type:Type.values()){
   * System.out.println(type);
   * }
   * }
   *
   * <pre>*/
  @Override
  public List<String> expectedBodyStmts() {
    return super.expectedBodyStmts(
        "this := @this: DeclareEnum",
        "l1 = staticinvoke <DeclareEnum$Type: DeclareEnum$Type[] values()>()",
        "l2 = lengthof l1",
        "l3 = 0",
        "label1:",
        "if l3 >= l2 goto label2",
        "l4 = l1[l3]",
        "$stack5 = <java.lang.System: java.io.PrintStream out>",
        "virtualinvoke $stack5.<java.io.PrintStream: void println(java.lang.Object)>(l4)",
        "l3 = l3 + 1",
        "goto label1",
        "label2:",
        "return");
  }
}
