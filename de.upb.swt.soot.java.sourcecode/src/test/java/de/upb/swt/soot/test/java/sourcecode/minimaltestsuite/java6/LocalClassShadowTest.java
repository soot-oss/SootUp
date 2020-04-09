package de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.java6;

import static org.junit.Assert.assertEquals;

import categories.Java8Test;
import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.common.ref.JInstanceFieldRef;
import de.upb.swt.soot.core.jimple.common.stmt.JAssignStmt;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.core.types.Type;
import de.upb.swt.soot.java.core.types.JavaClassType;
import de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.MinimalSourceTestSuiteBase;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(Java8Test.class)
public class LocalClassShadowTest extends MinimalSourceTestSuiteBase {

  JavaClassType localClass =
      identifierFactory.getClassType(
          getClassName(customTestWatcher.getClassPath()) + "1$LocalClass");
  SootClass sootLocalClass = loadClass(localClass);

  /** Test: OuterClass of LocalClass is LocalClassShadow */
  @Test
  public void testOuterClass() {
    assertEquals(getDeclaredClassSignature(), sootLocalClass.getOuterClass().get());
  }

  /** Test: How many Locals with ClassType {@link java.lang.String} */
  @Test
  public void testNumOfLocalsWithString() {
    SootMethod method = sootLocalClass.getMethod(getMethodSignature()).get();
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
    SootMethod method = sootLocalClass.getMethod(getMethodSignature()).get();
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
        "printInfo", localClass, "void", Collections.singletonList("java.lang.String"));
  }
}
