package de.upb.swt.soot.test.java.sourcecode.minimaltestsuite;

import static org.junit.Assert.*;

import categories.Java8Test;
import de.upb.swt.soot.core.DefaultIdentifierFactory;
import de.upb.swt.soot.core.Project;
import de.upb.swt.soot.core.frontend.AbstractClassSource;
import de.upb.swt.soot.core.inputlocation.AnalysisInputLocation;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.*;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.core.types.JavaClassType;
import de.upb.swt.soot.core.views.View;
import de.upb.swt.soot.java.sourcecode.inputlocation.JavaSourcePathAnalysisInputLocation;
import de.upb.swt.soot.test.java.sourcecode.frontend.Utils;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * @author: Markus Schmidt,
 * @author: Hasitha Rajapakse
 * @author: Kaustubh Kelkar
 */
@Category(Java8Test.class)
public abstract class MinimalTestSuiteBase {

  static final String baseDir = "src/test/resources/minimaltestsuite/";
  protected DefaultIdentifierFactory identifierFactory = DefaultIdentifierFactory.getInstance();
  private View view;

  public abstract MethodSignature getMethodSignature();

  public abstract List<String> expectedBodyStmts();

  @Before
  public void init() {

    AnalysisInputLocation walaSource =
        new JavaSourcePathAnalysisInputLocation(
            Collections.singleton(
                baseDir + File.separator + getTestDirectoryName() + File.separator));
    de.upb.swt.soot.core.Project<AnalysisInputLocation> p = new Project<>(walaSource);
    view = p.createOnDemandView();
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
    Optional<AbstractClass<? extends AbstractClassSource>> sc = view.getClass(clazz);
    assertTrue("no matching class signature found", sc.isPresent());
    return (SootClass) sc.get();
  }

  public SootMethod loadMethod(List<String> expectedStmts, MethodSignature methodSignature) {
    Optional<AbstractClass<? extends AbstractClassSource>> cs =
        view.getClass(methodSignature.getDeclClassType());
    assertTrue("no matching class signature found", cs.isPresent());

    Optional<? extends Method> m = cs.get().getMethod(methodSignature);
    assertTrue("No matching method signature found", m.isPresent());
    SootMethod method = (SootMethod) m.get();
    Utils.print(method, false);
    Body body = method.getBody();
    assertNotNull(body);

    List<String> actualStmts =
        body.getStmts().stream()
            .map(Stmt::toString)
            .collect(Collectors.toCollection(ArrayList::new));

    assertEquals(expectedStmts, actualStmts);
    return method;
  }
}
