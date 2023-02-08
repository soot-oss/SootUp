package sootup.java.sourcecode.minimaltestsuite.java6;

import static org.junit.Assert.assertEquals;

import categories.Java8Test;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.ref.JInstanceFieldRef;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.core.types.Type;
import sootup.java.core.types.JavaClassType;
import sootup.java.sourcecode.minimaltestsuite.MinimalSourceTestSuiteBase;

@Category(Java8Test.class)
public class LocalClassShadowTest extends MinimalSourceTestSuiteBase {

  JavaClassType localClass =
      identifierFactory.getClassType(
          getClassName(customTestWatcher.getClassPath()) + "1$LocalClass");
  SootClass<?> sootLocalClass = loadClass(localClass);

  /** Test: OuterClass of LocalClass is LocalClassShadow */
  @Test
  public void testOuterClass() {
    assertEquals(getDeclaredClassSignature(), sootLocalClass.getOuterClass().get());
  }

  /** Test: How many Locals with ClassType {@link java.lang.String} */
  @Test
  public void testNumOfLocalsWithString() {
    SootMethod method = sootLocalClass.getMethod(getMethodSignature().getSubSignature()).get();
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
    SootMethod method = sootLocalClass.getMethod(getMethodSignature().getSubSignature()).get();
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
        localClass, "printInfo", "void", Collections.singletonList("java.lang.String"));
  }
}
