package de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.java6;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import categories.Java8Test;
import com.ibm.wala.util.collections.ArraySet;
import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.java.core.types.JavaClassType;
import de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.MinimalSourceTestSuiteBase;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(Java8Test.class)
public class NestedClassShadowTest extends MinimalSourceTestSuiteBase {

  JavaClassType nestedClass =
      identifierFactory.getClassType(
          getClassName(customTestWatcher.getClassPath()) + "$NestedClass");
  SootClass sootNestedClass = loadClass(nestedClass);

  /** Test: OuterClass of NestedClass is NestedClassShadow */
  @Test
  public void testOuterClass() {
    assertEquals(
        loadClass(getDeclaredClassSignature()), loadClass(sootNestedClass.getOuterClass().get()));
  }

  /** Test: How many Locals with ClassType {@link java.lang.String} */
  @Test
  public void testNumOfLocalsWithString() {
    SootMethod method = sootNestedClass.getMethod(getMethodSignature()).get();
    Body methodBody = method.getBody();
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
    SootMethod method = sootNestedClass.getMethod(getMethodSignature()).get();
    Body methodBody = method.getBody();
    List<Stmt> stmts = methodBody.getStmts();
    Set<String> clazzNames = new ArraySet<String>();
    for (Stmt stmt : stmts) {
      if (stmt.toString().contains("info")) {
        clazzNames.add(getClassName(stmt));
      }
    }
    assertTrue(clazzNames.size() > 1);
  }

  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "printInfo", nestedClass, "void", Collections.singletonList("java.lang.String"));
  }

  private String getClassName(Stmt stmt) {
    String s = stmt.toString();
    int angleBracket = s.indexOf('<');
    int colon = s.indexOf(':');
    return s.substring(angleBracket + 1, colon);
  }
}
