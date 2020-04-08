package de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.java6;

import static org.junit.Assert.assertTrue;

import categories.Java8Test;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.common.ref.FieldRef;
import de.upb.swt.soot.core.jimple.common.ref.JParameterRef;
import de.upb.swt.soot.core.jimple.common.stmt.JAssignStmt;
import de.upb.swt.soot.core.jimple.common.stmt.JIdentityStmt;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.signatures.FieldSignature;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.MinimalSourceTestSuiteBase;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(Java8Test.class)
public class SubClassShadowingTest extends MinimalSourceTestSuiteBase {

  /** Test: Locals--info are from different class */
  @Test
  public void testClassesOfStringLocalAreDifferent() {
    Body methodBody = loadMethod(getMethodSignature()).getBody();

    List<Stmt> stmts = methodBody.getStmts();
    Set<ClassType> clazzes = new HashSet<>();
    boolean parameterLocal = false;
    for (Stmt stmt : stmts) {
      if (stmt instanceof JAssignStmt) {
        final Value rightOp = ((JAssignStmt) stmt).getRightOp();
        if (rightOp instanceof FieldRef) {
          final FieldSignature fieldSignature = ((FieldRef) rightOp).getFieldSignature();
          if (fieldSignature.getName().equals("info")) {
            final ClassType declClassType = fieldSignature.getDeclClassType();
            clazzes.add(declClassType);
          }
        }
      } else if (stmt instanceof JIdentityStmt
          && ((JIdentityStmt) stmt).getRightOp() instanceof JParameterRef) {
        // "info" refers to parameter; but name information for Locals is currently not kept
        parameterLocal = true;
      }
    }
    assertTrue(parameterLocal);
    assertTrue(clazzes.size() == 2);
  }

  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "printInfo",
        getDeclaredClassSignature(),
        "void",
        Collections.singletonList("java.lang.String"));
  }
}
