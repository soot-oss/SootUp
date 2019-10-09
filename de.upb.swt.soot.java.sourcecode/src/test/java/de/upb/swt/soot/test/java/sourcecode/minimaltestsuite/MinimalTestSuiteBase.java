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

/** @author: Markus Schmidt Hasitha Rajapakse */
@Category(Java8Test.class)
public abstract class MinimalTestSuiteBase {

  static final String baseDir = "src/test/resources/minimaltestsuite/";
  protected DefaultIdentifierFactory identifierFactory = DefaultIdentifierFactory.getInstance();
  protected SootMethod method;

  public abstract MethodSignature getMethodSignature();

  public abstract List<String> expectedBodyStmts();

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
    test(expectedBodyStmts(), getMethodSignature());
  }

  public void test(List<String> expectedStmts, MethodSignature methodSignature) {

    WalaClassLoader loader =
        new WalaClassLoader(
            baseDir + File.separator + getTestDirectoryName() + File.separator, null);
    Optional<SootMethod> m = WalaClassLoaderTestUtils.getSootMethod(loader, methodSignature);

    assertTrue("No matching method signature found", m.isPresent());
    method = m.get();
    Utils.print(method, false);
    Body body = method.getBody();
    assertNotNull(body);

    List<String> actualStmts =
        body.getStmts().stream()
            .map(Stmt::toString)
            .collect(Collectors.toCollection(ArrayList::new));

    assertEquals(expectedStmts, actualStmts);
  }

  public void checkClassModifier(String modifier) {
    WalaClassLoader loader =
        new WalaClassLoader(
            baseDir + File.separator + getTestDirectoryName() + File.separator, null);
    Optional<ClassSource> cs = loader.getClassSource(getDeclaredClassSignature());
    assertTrue("no matching class signature found", cs.isPresent());
    ClassSource classSource = cs.get();
    SootClass sootClass = new SootClass(classSource, SourceType.Application);
    switch (modifier) {
      case "PUBLIC":
        assertTrue(sootClass.isPublic());
        break;
      case "PRIVATE":
        assertTrue(sootClass.isPrivate());
        break;
      case "PROTECTED":
        assertTrue(sootClass.isProtected());
        break;
      case "":
        assertEquals(classSource.resolveModifiers().toString(), "[]");
        break;
      default:
        throw new IllegalStateException("Unexpected value: " + modifier);
    }
  }

  public void isAbstractClass() {
    WalaClassLoader loader =
        new WalaClassLoader(
            baseDir + File.separator + getTestDirectoryName() + File.separator, null);
    Optional<ClassSource> cs = loader.getClassSource(getDeclaredClassSignature());
    assertTrue("no matching class signature found", cs.isPresent());
    ClassSource classSource = cs.get();
    SootClass sootClass = new SootClass(classSource, SourceType.Application);
    /** get abstract class details assuming abstract class is extended by the tested class */
    Optional<JavaClassType> parentClass = sootClass.getSuperclass();
    Optional<ClassSource> cs2 = loader.getClassSource(parentClass.get());
    assertTrue("no matching class signature found", cs.isPresent());
    ClassSource classSource2 = cs2.get();
    SootClass sootClass2 = new SootClass(classSource2, SourceType.Application);
    assertTrue(sootClass2.isAbstract());
  }

  public void checkMethodModifier(String modifier, MethodSignature methodSignature) {
    WalaClassLoader loader =
        new WalaClassLoader(
            baseDir + File.separator + getTestDirectoryName() + File.separator, null);
    Optional<SootMethod> m = WalaClassLoaderTestUtils.getSootMethod(loader, methodSignature);
    assertTrue("No matching method signature found", m.isPresent());
    SootMethod method = m.get();
    switch (modifier) {
      case "PUBLIC":
        assertTrue(method.isPublic());
        break;
      case "PRIVATE":
        assertTrue(method.isPrivate());
        break;
      case "PROTECTED":
        assertTrue(method.isProtected());
        break;
      case "":
        assertEquals(method.getModifiers().toString(), "[]");
        break;
      default:
        throw new IllegalStateException("Unexpected value: " + modifier);
    }
  }
}
