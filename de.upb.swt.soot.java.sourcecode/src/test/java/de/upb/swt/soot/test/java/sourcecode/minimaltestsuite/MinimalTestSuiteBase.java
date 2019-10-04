package de.upb.swt.soot.test.java.sourcecode.minimaltestsuite;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import categories.Java8Test;
import de.upb.swt.soot.core.DefaultIdentifierFactory;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.core.types.JavaClassType;
import de.upb.swt.soot.java.sourcecode.frontend.WalaClassLoader;
import de.upb.swt.soot.test.java.sourcecode.frontend.Utils;
import de.upb.swt.soot.test.java.sourcecode.frontend.WalaClassLoaderTestUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/** @author: Markus Schmidt */
@Category(Java8Test.class)
public abstract class MinimalTestSuiteBase {

  static final String baseDir = "src/test/resources/minimaltestsuite/";
  protected DefaultIdentifierFactory identifierFactory = DefaultIdentifierFactory.getInstance();

  public abstract MethodSignature getMethodSignature();

  public abstract List<String> getJimpleLines();

  /**
   * @returns the name of the parent directory - assuming the directory structure is only one level
   *     deep
   */
  public String getTestDirectoryName() {
    String canonicalName = this.getClass().getCanonicalName();
    canonicalName =
        canonicalName.substring(
            0, canonicalName.length() - this.getClass().getSimpleName().length() - 1);
    canonicalName = canonicalName.substring(canonicalName.lastIndexOf('.') + 1);
    return canonicalName;
  }

  /**
   * @returns the name of the class - assuming the testname unit has "Test" appended to the
   *     respective name of the class
   */
  public String getClassName() {
    // remove "test" from the end of the testName
    return this.getClass()
        .getSimpleName()
        .substring(0, this.getClass().getSimpleName().length() - 4);
  }

  protected JavaClassType getDeclaredClassSignature() {
    return identifierFactory.getClassType(getClassName());
  }

  @Test
  public void test() {

    WalaClassLoader loader =
        new WalaClassLoader(
            baseDir
                + File.separator
                + getTestDirectoryName()
                + File.separator
                + getClassName()
                + File.separator,
            null);
    MethodSignature methodSignature = getMethodSignature();
    Optional<SootMethod> m = WalaClassLoaderTestUtils.getSootMethod(loader, methodSignature);

    assertTrue(m.isPresent());
    SootMethod method = m.get();
    Utils.print(method, false);
    Body body = method.getBody();
    assertNotNull(body);

    List<String> actualStmts =
        body.getStmts().stream()
            .map(Stmt::toString)
            .collect(Collectors.toCollection(ArrayList::new));

    List<String> expectedStmts = getJimpleLines();
    assertEquals(expectedStmts, actualStmts);
  }
}
