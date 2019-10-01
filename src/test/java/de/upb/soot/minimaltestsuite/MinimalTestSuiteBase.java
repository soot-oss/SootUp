package de.upb.soot.minimaltestsuite;

import static org.junit.Assert.*;

import categories.Java8Test;
import de.upb.soot.DefaultIdentifierFactory;
import de.upb.soot.core.Body;
import de.upb.soot.core.SootMethod;
import de.upb.soot.frontends.java.Utils;
import de.upb.soot.frontends.java.WalaClassLoader;
import de.upb.soot.frontends.java.WalaClassLoaderTestUtils;
import de.upb.soot.jimple.common.stmt.Stmt;
import de.upb.soot.signatures.MethodSignature;
import de.upb.soot.types.JavaClassType;
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
