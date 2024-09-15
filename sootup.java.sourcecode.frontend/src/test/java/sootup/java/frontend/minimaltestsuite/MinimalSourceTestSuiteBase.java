package sootup.java.frontend.minimaltestsuite;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.model.Body;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.core.util.Utils;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.JavaSootClass;
import sootup.java.core.types.JavaClassType;
import sootup.java.core.views.JavaView;
import sootup.java.frontend.inputlocation.JavaSourcePathAnalysisInputLocation;

/**
 * @author Markus Schmidt
 * @author Hasitha Rajapakse
 * @author Kaustubh Kelkar
 */
@Tag("Java8")
public abstract class MinimalSourceTestSuiteBase {

  static final String baseDir = "../shared-test-resources/miniTestSuite/";
  protected JavaIdentifierFactory identifierFactory = JavaIdentifierFactory.getInstance();
  private static String testDir = "";
  private static JavaView javaView;

  protected String testedClassName = "";

  /** Load WalaClassLoader once for each test directory */
  @BeforeEach
  protected void init() {
    testedClassName = getClassName(this.getClass().getSimpleName());
    String currentTestDir = getTestDirectoryName(this.getClass().getCanonicalName());
    if (!testDir.equals(currentTestDir)) {
      testDir = currentTestDir;
      AnalysisInputLocation inputLocation =
          new JavaSourcePathAnalysisInputLocation(
              baseDir
                  + File.separator
                  + currentTestDir
                  + File.separator
                  + "source"
                  + File.separator);
      javaView = new JavaView(inputLocation);
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
    System.out.println(classPath);
    String[] classPathArray = classPath.split("\\.");
    String className =
        classPathArray[classPathArray.length - 1].substring(
            0, classPathArray[classPathArray.length - 1].length() - 4);
    return className;
  }

  protected JavaClassType getDeclaredClassSignature() {
    return identifierFactory.getClassType(testedClassName);
  }

  public JavaSootClass loadClass(ClassType clazz) {
    Optional<JavaSootClass> cs = javaView.getClass(clazz);
    assertTrue(cs.isPresent(), "no matching class signature found");
    return cs.get();
  }

  public JavaSootClass loadClass(String className) {
    ClassType cs = identifierFactory.getClassType(className);
    return loadClass(cs);
  }

  public SootMethod loadMethod(MethodSignature methodSignature) {
    JavaSootClass clazz = loadClass(methodSignature.getDeclClassType());
    Optional<? extends SootMethod> m = clazz.getMethod(methodSignature.getSubSignature());
    assertTrue(m.isPresent(), "No matching method signature found");
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
