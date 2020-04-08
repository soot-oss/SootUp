package de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.java6;

import static org.junit.Assert.assertTrue;

import categories.Java8Test;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.common.ref.FieldRef;
import de.upb.swt.soot.core.jimple.common.stmt.JAssignStmt;
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
    for (Stmt stmt : stmts) {
      if (stmt instanceof JAssignStmt) {
        final Value rightOp = ((JAssignStmt) stmt).getRightOp();
        if (rightOp instanceof FieldRef) {
          final FieldSignature fieldSignature = ((FieldRef) rightOp).getFieldSignature();
          if (fieldSignature.getName() == "info") {
            final ClassType declClassType = fieldSignature.getDeclClassType();
            clazzes.add(declClassType);
          }
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
}
