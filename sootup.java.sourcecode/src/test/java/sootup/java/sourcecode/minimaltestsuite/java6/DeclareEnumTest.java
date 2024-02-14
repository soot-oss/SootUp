/** @author: Hasitha Rajapakse */
package sootup.java.sourcecode.minimaltestsuite.java6;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.java.sourcecode.minimaltestsuite.MinimalSourceTestSuiteBase;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("Java8")
public class DeclareEnumTest extends MinimalSourceTestSuiteBase {

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
        "r0 := @this: DeclareEnum",
        "r1 = staticinvoke <DeclareEnum$Type: DeclareEnum$Type[] values()>()",
        "i0 = 0",
        "label1:",
        "i1 = lengthof r1",
        "z0 = i0 < i1",
        "if z0 == 0 goto label2",
        "r2 = r1[i0]",
        "r3 = <java.lang.System: java.io.PrintStream out>",
        "virtualinvoke r3.<java.io.PrintStream: void println(java.lang.Object)>(r2)",
        "i2 = i0",
        "i3 = i0 + 1",
        "i0 = i3",
        "goto label1",
        "label2:",
        "return");
  }
}
