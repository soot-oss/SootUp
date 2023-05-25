package sootup.java.sourcecode.minimaltestsuite;

import static org.junit.Assert.*;

import categories.Java8Test;
import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.ClassRule;
import org.junit.experimental.categories.Category;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import sootup.core.model.Body;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.core.util.Utils;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.JavaProject;
import sootup.java.core.JavaSootClass;
import sootup.java.core.language.JavaLanguage;
import sootup.java.core.types.JavaClassType;
import sootup.java.core.views.JavaView;
import sootup.java.sourcecode.inputlocation.JavaSourcePathAnalysisInputLocation;

/**
 * @author Markus Schmidt
 * @author Hasitha Rajapakse
 * @author Kaustubh Kelkar
 */
@Category(Java8Test.class)
public abstract class MinimalSourceTestSuiteBase {

  static final String baseDir = "../shared-test-resources/miniTestSuite/";
  protected JavaIdentifierFactory identifierFactory = JavaIdentifierFactory.getInstance();

  @ClassRule public static CustomTestWatcher customTestWatcher = new CustomTestWatcher();

  public static class CustomTestWatcher extends TestWatcher {
    private String classPath = MinimalSourceTestSuiteBase.class.getSimpleName();
    private JavaView javaView;

    /** Load WalaClassLoader once for each test directory */
    @Override
    protected void starting(Description description) {
      String prevClassDirName = getTestDirectoryName(getClassPath());
      setClassPath(description.getClassName());
      if (!prevClassDirName.equals(getTestDirectoryName(getClassPath()))) {
        JavaProject project =
            JavaProject.builder(new JavaLanguage(8))
                .addInputLocation(
                    new JavaSourcePathAnalysisInputLocation(
                        baseDir
                            + File.separator
                            + getTestDirectoryName(getClassPath())
                            + File.separator
                            + "source"
                            + File.separator))
                .build();
        javaView = project.createView();
        setJavaView(javaView);
      }
    }

    public String getClassPath() {
      return classPath;
    }

    private void setClassPath(String classPath) {
      this.classPath = classPath;
    }

    private void setJavaView(JavaView javaView) {
      this.javaView = javaView;
    }

    public JavaView getJavaView() {
      return javaView;
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
   * @return the name of the parent directory - assuming the directory structure is only one level
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
   * @return the name of the class - assuming the testname unit has "Test" appended to the
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

  public JavaSootClass loadClass(ClassType clazz) {
    Optional<JavaSootClass> cs = customTestWatcher.getJavaView().getClass(clazz);
    assertTrue("no matching class signature found", cs.isPresent());
    return cs.get();
  }

  public SootMethod loadMethod(MethodSignature methodSignature) {
    JavaSootClass clazz = loadClass(methodSignature.getDeclClassType());
    Optional<? extends SootMethod> m = clazz.getMethod(methodSignature.getSubSignature());
    assertTrue("No matching method signature found", m.isPresent());
    return m.get();
  }

  public void assertJimpleStmts(SootMethod method, List<String> expectedStmts) {
    Body body = method.getBody();
    assertNotNull(body);

    List<String> actualStmts = Utils.bodyStmtsAsStrings(body);

    if (!expectedStmts.equals(actualStmts)) {
      System.out.println(Utils.generateJimpleTest(Utils.filterJimple(actualStmts.stream())));
      assertEquals(expectedStmts, actualStmts);
    }
  }

  public List<String> expectedBodyStmts(String... jimpleLines) {
    return Stream.of(jimpleLines).collect(Collectors.toList());
  }
}
