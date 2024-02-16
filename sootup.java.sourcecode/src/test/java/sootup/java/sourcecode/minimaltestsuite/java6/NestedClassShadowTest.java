package sootup.java.sourcecode.minimaltestsuite.java6;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.ref.JInstanceFieldRef;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.core.types.Type;
import sootup.java.sourcecode.minimaltestsuite.MinimalSourceTestSuiteBase;

@Tag("Java8")
public class NestedClassShadowTest extends MinimalSourceTestSuiteBase {

  /** Test: OuterClass of NestedClass is NestedClassShadow */
  @Test
  public void testOuterClass() {
    assertEquals(getDeclaredClassSignature(), loadClass(genNestedClassType()).getOuterClass().get());
  }

  /** Test: How many Locals with ClassType {@link java.lang.String} */
  @Test
  public void testNumOfLocalsWithString() {
    SootMethod method = loadMethod(getMethodSignature());
    Body methodBody = method.getBody();
    Set<Local> locals = methodBody.getLocals();
    Set<Local> stringLocals =
        locals.stream()
            .filter(local -> local.getType().toString().equals("java.lang.String"))
            .collect(Collectors.toSet());
    assertEquals(3, stringLocals.size());
  }

  /** Test: Locals--info are from different classes */
  @Test
  public void testClassesOfStringLocalAreDifferent() {
    SootMethod method = loadMethod(getMethodSignature());
    Body methodBody = method.getBody();
    List<Stmt> stmts = methodBody.getStmts();
    Set<Type> classTypes = new HashSet<Type>();
    for (Stmt stmt : stmts) {
      if (stmt instanceof JAssignStmt) {
        final Value rightOp = ((JAssignStmt) stmt).getRightOp();
        if (rightOp instanceof JInstanceFieldRef) {
          final ClassType declClassType =
              ((JInstanceFieldRef) rightOp).getFieldSignature().getDeclClassType();
          classTypes.add(declClassType);
        }
      }
    }
    assertEquals(2, classTypes.size());
  }

  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        genNestedClassType(), "printInfo", "void", Collections.singletonList("java.lang.String"));
  }

  private ClassType genNestedClassType(){
    return identifierFactory.getClassType(testedClassName + "$NestedClass");
  }
}
