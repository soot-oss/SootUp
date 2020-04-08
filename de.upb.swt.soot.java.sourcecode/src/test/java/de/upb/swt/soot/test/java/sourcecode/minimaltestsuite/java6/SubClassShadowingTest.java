package de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.java6;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import categories.Java8Test;
import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.common.ref.FieldRef;
import de.upb.swt.soot.core.jimple.common.stmt.JAssignStmt;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.MinimalSourceTestSuiteBase;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(Java8Test.class)
public class SubClassShadowingTest extends MinimalSourceTestSuiteBase {

  SootMethod sootMethod = loadMethod(getMethodSignature());
  Body methodBody = sootMethod.getBody();

  /** Test: How many Locals with ClassType {@link java.lang.String} */
  @Test
  public void testNumOfLocalsWithString() {
    Set<Local> locals = methodBody.getLocals();
    Set<Local> stringLocals =
        locals.stream()
            .filter(local -> local.getType().toString().equals("java.lang.String"))
            .collect(Collectors.toSet());
    assertEquals(3, stringLocals.size());
  }

  /** Test: Locals--info are from different class */
  @Test
  public void testClassesOfStringLocalAreDifferent() {
    List<Stmt> stmts = methodBody.getStmts();
    Set<ClassType> clazzes = new HashSet<>();
    for (Stmt stmt : stmts) {
      if (stmt instanceof JAssignStmt) {
        final Value rightOp = ((JAssignStmt) stmt).getRightOp();
        if (rightOp instanceof FieldRef) {
          final ClassType declClassType =
              ((FieldRef) rightOp).getFieldSignature().getDeclClassType();
          clazzes.add(declClassType);
        }
      }
    }
    assertTrue(clazzes.size() == 3);
  }

  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "printInfo",
        getDeclaredClassSignature(),
        "void",
        Collections.singletonList("java.lang.String"));
  }

  private String getClassName(Stmt stmt) {
    String s = stmt.toString();
    int angleBracket = s.indexOf('<');
    int colon = s.indexOf(':');
    return s.substring(angleBracket + 1, colon);
  }
}
