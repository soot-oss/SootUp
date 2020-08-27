package de.upb.swt.soot.test.java.bytecode.minimaltestsuite.java6;

import static org.junit.Assert.assertTrue;

import categories.Java8Test;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.java.core.JavaIdentifierFactory;
import de.upb.swt.soot.test.java.bytecode.minimaltestsuite.MinimalBytecodeTestSuiteBase;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/** @author Kaustubh Kelkar */
@Category(Java8Test.class)
public class DeclareEnumTest extends MinimalBytecodeTestSuiteBase {

  @Test
  public void test() {
    SootClass sc =
        loadClass(
            JavaIdentifierFactory.getInstance()
                .getClassType(getDeclaredClassSignature().getFullyQualifiedName() + "$Type"));
    assertTrue(sc.isEnum());
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
  }

  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "declareEnum", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  /**  <pre>
   * public void declareEnum(){
   * for(Type type:Type.values()){
   * System.out.println(type);
   * }
   * }
   * }
   *
   * <pre>*/
  @Override
  public List<String> expectedBodyStmts() {
    return super.expectedBodyStmts(
        "l0 := @this: DeclareEnum",
        "$stack5 = staticinvoke <DeclareEnum$Type: DeclareEnum$Type[] values()>()",
        "l1 = $stack5",
        "l2 = lengthof l1",
        "l3 = 0",
        "label1:",
        "$stack8 = l3",
        "$stack7 = l2",
        "if $stack8 >= $stack7 goto label2",
        "l4 = l1[l3]",
        "$stack6 = <java.lang.System: java.io.PrintStream out>",
        "virtualinvoke $stack6.<java.io.PrintStream: void println(java.lang.Object)>(l4)",
        "l3 = l3 + 1",
        "goto label1",
        "label2:",
        "return");
  }
}
