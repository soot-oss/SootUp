package de.upb.swt.soot.test.java.sourcecode.minimaltestsuite;

import static org.junit.Assert.*;

import categories.Java8Test;
import de.upb.swt.soot.core.DefaultIdentifierFactory;
import de.upb.swt.soot.core.frontend.ClassSource;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.model.SourceType;
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

/**
 * @author: Markus Schmidt,
 * @author: Hasitha Rajapakse
 */
@Category(Java8Test.class)
public abstract class MinimalTestSuiteBase {

  static final String baseDir = "src/test/resources/minimaltestsuite/";
  protected DefaultIdentifierFactory identifierFactory = DefaultIdentifierFactory.getInstance();

  public MethodSignature getMethodSignature() {
    fail("getMethodSignature() is used but not overridden");
    return null;
  }

  public List<String> expectedBodyStmts() {
    fail("expectedBodyStmts() is used but not overridden");
    return null;
  }

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
    String substring =
        this.getClass().getSimpleName().substring(0, this.getClass().getSimpleName().length() - 4);

    return substring;
  }

  protected JavaClassType getDeclaredClassSignature() {
    return identifierFactory.getClassType(getClassName());
  }

  @Test
  public void defaultTest() {
    loadMethod(expectedBodyStmts(), getMethodSignature());
  }

  public SootClass loadClass(JavaClassType clazz) {
    WalaClassLoader loader =
        new WalaClassLoader(
            baseDir + File.separator + getTestDirectoryName() + File.separator, null);
    Optional<ClassSource> cs = loader.getClassSource(clazz);
    assertTrue("no matching class signature found", cs.isPresent());
    ClassSource classSource = cs.get();
    return new SootClass(classSource, SourceType.Application);
  }

  public SootMethod loadMethod(List<String> expectedStmts, MethodSignature methodSignature) {

    WalaClassLoader loader =
        new WalaClassLoader(
            baseDir + File.separator + getTestDirectoryName() + File.separator, null);
    Optional<SootMethod> m = WalaClassLoaderTestUtils.getSootMethod(loader, methodSignature);

    assertTrue("No matching method signature found", m.isPresent());
    SootMethod method = m.get();
    Utils.print(method, false);
    assertJimpleStmts(method, expectedStmts);
    return method;
  }

  public void assertJimpleStmts(SootMethod method, List<String> expectedStmts) {
    Body body = method.getBody();
    assertNotNull(body);

    List<String> actualStmts =
        body.getStmts().stream()
            .map(Stmt::toString)
            .collect(Collectors.toCollection(ArrayList::new));

    assertEquals(expectedStmts, actualStmts);
  }
}
