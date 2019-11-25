package de.upb.swt.soot.test.java.sourcecode.minimaltestsuite;

import static org.junit.Assert.*;

import categories.Java8Test;
import de.upb.swt.soot.core.frontend.ClassSource;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.model.SourceType;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.java.core.JavaIdentifierFactory;
import de.upb.swt.soot.java.core.types.JavaClassType;
import de.upb.swt.soot.java.sourcecode.frontend.WalaClassLoader;
import de.upb.swt.soot.test.java.sourcecode.frontend.Utils;
import de.upb.swt.soot.test.java.sourcecode.frontend.WalaClassLoaderTestUtils;
import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

/**
 * @author: Markus Schmidt,
 * @author: Hasitha Rajapakse
 */
@Category(Java8Test.class)
public abstract class MinimalTestSuiteBase {

  static final String baseDir = "src/test/resources/minimaltestsuite/";
  protected JavaIdentifierFactory identifierFactory = JavaIdentifierFactory.getInstance();

  @ClassRule public static CustomTestWatcher customTestWatcher = new CustomTestWatcher();

  public static class CustomTestWatcher extends TestWatcher {
    private String classPath = MinimalTestSuiteBase.class.getSimpleName();
    private WalaClassLoader loader;

    /** Load WalaClassLoader once for each test directory */
    @Override
    protected void starting(Description description) {
      String prevClassDirName = getTestDirectoryName(getClassPath());
      setClassPath(description.getClassName());
      if (!prevClassDirName.equals(getTestDirectoryName(getClassPath()))) {
        WalaClassLoader loader =
            new WalaClassLoader(
                baseDir + File.separator + getTestDirectoryName(getClassPath()) + File.separator,
                null);
        setLoader(loader);
      }
    }

    public String getClassPath() {
      return classPath;
    }

    private void setClassPath(String classPath) {
      this.classPath = classPath;
    }

    private void setLoader(WalaClassLoader loader) {
      this.loader = loader;
    }

    public WalaClassLoader getLoader() {
      return loader;
    }
  }

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
  public static String getTestDirectoryName(String classPath) {
    String[] classPathArray = classPath.split("\\.");
    String testDirectoryName = "";
    if (classPathArray.length > 1) {
      testDirectoryName = classPathArray[classPathArray.length - 2];
    }
    return testDirectoryName;
  }

  /**
   * @returns the name of the class - assuming the testname unit has "Test" appended to the
   *     respective name of the class
   */
  public String getClassName(String classPath) {
    String[] classPathArray = classPath.split("\\.");
    String className =
        classPathArray[classPathArray.length - 1].substring(
            0, classPathArray[classPathArray.length - 1].length() - 4);
    return className;
  }

  protected JavaClassType getDeclaredClassSignature() {
    return identifierFactory.getClassType(getClassName(customTestWatcher.classPath));
  }

  @Test
  public void defaultTest() {
    loadMethod(expectedBodyStmts(), getMethodSignature());
  }

  public SootClass loadClass(ClassType clazz) {
    Optional<ClassSource> cs = customTestWatcher.getLoader().getClassSource(clazz);
    assertTrue("no matching class signature found", cs.isPresent());
    ClassSource classSource = cs.get();
    return new SootClass(classSource, SourceType.Application);
  }

  public SootMethod loadMethod(List<String> expectedStmts, MethodSignature methodSignature) {
    Optional<SootMethod> m =
        WalaClassLoaderTestUtils.getSootMethod(customTestWatcher.getLoader(), methodSignature);

    assertTrue("No matching method signature found", m.isPresent());
    SootMethod method = m.get();
    Utils.print(method, false);
    assertJimpleStmts(method, expectedStmts);
    return method;
  }

  public void assertJimpleStmts(SootMethod method, List<String> expectedStmts) {
    Body body = method.getBody();
    assertNotNull(body);

    List<String> actualStmts = Utils.bodyStmtsAsStrings(body);

    assertEquals(expectedStmts, actualStmts);
  }

  public List<String> expectedBodyStmts(String... jimpleLines) {
    return Stream.of(jimpleLines).collect(Collectors.toList());
  }
}
