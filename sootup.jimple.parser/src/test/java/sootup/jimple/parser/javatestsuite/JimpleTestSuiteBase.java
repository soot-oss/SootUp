package sootup.jimple.parser.javatestsuite;

import static org.junit.Assert.*;

import java.nio.file.Paths;
import java.util.*;
import org.junit.Before;
import org.junit.experimental.categories.Category;
import sootup.core.model.Body;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.core.util.Utils;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.types.JavaClassType;
import sootup.jimple.parser.JimpleAnalysisInputLocation;
import sootup.jimple.parser.JimpleProject;
import sootup.jimple.parser.JimpleView;
import sootup.jimple.parser.categories.Java8Test;

/** @author Markus Schmidt */
@Category(Java8Test.class)
public abstract class JimpleTestSuiteBase {

  static final String baseDir = "src/test/java/resources/jimple/";
  protected JavaIdentifierFactory identifierFactory = JavaIdentifierFactory.getInstance();
  private JimpleView view;

  @Before
  public void setup() {
    view = new JimpleProject(new JimpleAnalysisInputLocation<>(Paths.get(baseDir))).createView();
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

  public SootClass<?> loadClass(ClassType clazz) {

    Optional<SootClass<?>> cs = view.getClass(clazz);
    assertTrue("no matching class for " + clazz + " found", cs.isPresent());
    return cs.get();
  }

  public SootMethod loadMethod(MethodSignature methodSignature) {
    SootClass<?> clazz = loadClass(methodSignature.getDeclClassType());
    Optional<? extends SootMethod> m = clazz.getMethod(methodSignature.getSubSignature());
    if (!m.isPresent()) {
      System.out.println("existing methods:");
      clazz.getMethods().forEach(System.out::println);
    }
    assertTrue("No matching method for " + methodSignature + " found", m.isPresent());
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
