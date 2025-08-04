package sootup.java.bytecode.frontend.minimaltestsuite;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.model.Body;
import sootup.core.model.SootMethod;
import sootup.core.model.SourceType;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.core.util.Utils;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.JavaSootClass;
import sootup.java.core.JavaSootMethod;
import sootup.java.core.types.JavaClassType;
import sootup.java.core.views.JavaView;

/**
 * @author Markus Schmidt,
 * @author Hasitha Rajapakse
 * @author Kaustubh Kelkar
 */
public abstract class MinimalBytecodeTestSuiteBase {

  static final String baseDir = "../shared-test-resources/miniTestSuite";
  protected JavaIdentifierFactory identifierFactory = JavaIdentifierFactory.getInstance();

  private static String testDir = "";
  private static JavaView javaView;

  protected String testedClassName = "";

  @BeforeEach
  protected void init() {
    testedClassName = getClassName(this.getClass().getSimpleName());
    String currentTestDir = getTestDirectoryName(this.getClass().getCanonicalName());
    if (!testDir.equals(currentTestDir)) {
      testDir = currentTestDir;
      AnalysisInputLocation inputLocation =
          new JavaClassPathAnalysisInputLocation(
              baseDir
                  + File.separator
                  + currentTestDir
                  + File.separator
                  + "binary"
                  + File.separator,
              SourceType.Application,
              Collections.emptyList());
      javaView = new JavaView(inputLocation);
    }
  }

  public static JavaView getJavaView() {
    return javaView;
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
    return classPathArray[classPathArray.length - 1].substring(
        0, classPathArray[classPathArray.length - 1].length() - 4);
  }

  protected JavaClassType getDeclaredClassSignature() {
    return identifierFactory.getClassType(testedClassName);
  }

  public JavaSootClass loadClass(ClassType clazz) {
    Optional<JavaSootClass> cs = javaView.getClass(clazz);
    assertTrue(cs.isPresent(), "No matching class signature found");
    return cs.get();
  }

  public JavaSootMethod loadMethod(MethodSignature methodSignature) {
    JavaSootClass clazz = loadClass(methodSignature.getDeclClassType());
    Optional<JavaSootMethod> m = clazz.getMethod(methodSignature.getSubSignature());
    assertTrue(m.isPresent(), "No matching method signature found");
    return m.get();
  }

  public void assertJimpleStmts(SootMethod method, List<String> expectedStmts) {
    Body body = method.getBody();
    assertNotNull(body);
    List<String> actualStmts = Utils.bodyStmtsAsStrings(body);
    if (!expectedStmts.equals(actualStmts)) {
      System.out.println(Utils.generateJimpleTest(actualStmts));
      assertEquals(expectedStmts, actualStmts);
    }
  }

  public List<String> expectedBodyStmts(String... jimpleLines) {
    return Stream.of(jimpleLines).collect(Collectors.toList());
  }
}
