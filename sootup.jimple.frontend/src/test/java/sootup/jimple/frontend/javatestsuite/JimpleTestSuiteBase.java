package sootup.jimple.frontend.javatestsuite;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import sootup.core.model.Body;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.core.util.Utils;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.types.JavaClassType;
import sootup.jimple.frontend.JimpleAnalysisInputLocation;
import sootup.jimple.frontend.JimpleView;

/**
 * @author Markus Schmidt
 */
public abstract class JimpleTestSuiteBase {

  static final String baseDir = "src/test/java/resources/jimple/";
  protected JavaIdentifierFactory identifierFactory = JavaIdentifierFactory.getInstance();
  private JimpleView view;

  @BeforeEach
  public void setup() {
    view = new JimpleView(new JimpleAnalysisInputLocation(Paths.get(baseDir)));
  }

  /**
   * @return the name of the parent directory - assuming the directory structure is only one level
   *     deep
   */
  public static String getTestDirectoryName(String classOfPath) {
    String[] classPathArray = classOfPath.split("\\.");
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
  public String deriveClassName(String classPath) {
    String[] classPathArray = classPath.split("\\.");
    String className =
        classPathArray[classPathArray.length - 1].substring(
            0, classPathArray[classPathArray.length - 1].length() - 4);
    return className;
  }

  protected JavaClassType getDeclaredClassSignature() {
    return identifierFactory.getClassType(deriveClassName(this.getClass().getSimpleName()));
  }

  public SootClass loadClass(ClassType clazz) {

    Optional<SootClass> cs = view.getClass(clazz);
    assertTrue(cs.isPresent(), "no matching class for " + clazz + " found");
    return cs.get();
  }

  public SootMethod loadMethod(MethodSignature methodSignature) {
    SootClass clazz = loadClass(methodSignature.getDeclClassType());
    Optional<? extends SootMethod> m = clazz.getMethod(methodSignature.getSubSignature());
    if (!m.isPresent()) {
      System.out.println("existing methods:");
      clazz.getMethods().forEach(System.out::println);
    }
    assertTrue(m.isPresent(), "No matching method for " + methodSignature + " found");
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
}
