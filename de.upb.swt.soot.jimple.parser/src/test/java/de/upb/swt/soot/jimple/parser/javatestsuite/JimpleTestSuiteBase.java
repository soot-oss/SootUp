package de.upb.swt.soot.jimple.parser.javatestsuite;

import static org.junit.Assert.*;

import de.upb.swt.soot.core.inputlocation.AnalysisInputLocation;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.core.util.Utils;
import de.upb.swt.soot.core.views.View;
import de.upb.swt.soot.java.core.JavaIdentifierFactory;
import de.upb.swt.soot.java.core.types.JavaClassType;
import de.upb.swt.soot.jimple.parser.JimpleAnalysisInputLocation;
import de.upb.swt.soot.jimple.parser.JimpleProject;
import de.upb.swt.soot.jimple.parser.categories.Java8Test;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Before;
import org.junit.experimental.categories.Category;

/** @author Markus Schmidt */
@Category(Java8Test.class)
public abstract class JimpleTestSuiteBase {

  static final String baseDir = "src/test/java/resources/";
  protected JavaIdentifierFactory identifierFactory = JavaIdentifierFactory.getInstance();
  private View view;

  @Before
  public void setup() {
    AnalysisInputLocation inputLocation =
        new JimpleAnalysisInputLocation(
            Paths.get(baseDir + getTestDirectoryName(getClass().getCanonicalName())));
    view = new JimpleProject(inputLocation).createOnDemandView();
  }

  /**
   * @returns the name of the parent directory - assuming the directory structure is only one level
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
   * @returns the name of the class - assuming the testname unit has "Test" appended to the
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

    Optional<SootClass> cs = (Optional<SootClass>) view.getClass(clazz);
    assertTrue("no matching class signature found", cs.isPresent());
    return cs.get();
  }

  public SootMethod loadMethod(MethodSignature methodSignature) {
    SootClass clazz = loadClass(methodSignature.getDeclClassType());
    Optional<SootMethod> m = clazz.getMethod(methodSignature);
    assertTrue("No matching method for the signature found", m.isPresent());
    SootMethod method = m.get();
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
