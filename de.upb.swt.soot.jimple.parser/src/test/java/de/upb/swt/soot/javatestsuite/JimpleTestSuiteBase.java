package de.upb.swt.soot.javatestsuite;

import static org.junit.Assert.*;

import de.upb.swt.soot.JimpleAnalysisInputLocation;
import de.upb.swt.soot.categories.Java8Test;
import de.upb.swt.soot.core.Project;
import de.upb.swt.soot.core.frontend.AbstractClassSource;
import de.upb.swt.soot.core.model.AbstractClass;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.core.util.Utils;
import de.upb.swt.soot.core.util.printer.Printer;
import de.upb.swt.soot.core.views.View;
import de.upb.swt.soot.java.core.JavaIdentifierFactory;
import de.upb.swt.soot.java.core.JavaProject;
import de.upb.swt.soot.java.core.language.JavaLanguage;
import de.upb.swt.soot.java.core.types.JavaClassType;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/** @author Markus Schmidt */
@Category(Java8Test.class)
public abstract class JimpleTestSuiteBase {

  static final String baseDir = "../shared-test-resources/jimpletestsuite/java/";
  protected JavaIdentifierFactory identifierFactory = JavaIdentifierFactory.getInstance();
  private View view;

  /** Helper to save Jimple files */
  @Test
  void saveFilesToJimple() {

    String inputDir = "../shared-test-resources/minimaltestsuite/";
    Set<String> locationSet =
        new HashSet(
            Arrays.asList(
                inputDir + "java6", inputDir + "java7", inputDir + "java8"
                // inputDir + "java9", inputDir + "java10"
                ));

    Project project =
        JavaProject.builder(new JavaLanguage(8))
            .addClassPath(new JimpleAnalysisInputLocation(locationSet))
            .build();
    view = project.createOnDemandView();

    //  list java minimal examples
    for (AbstractClass<? extends AbstractClassSource> acl : view.getClasses()) {

      SootClass cl = (SootClass) acl;
      // build outputdirectory name
      final String pckgname = cl.getType().getPackageName().toString();
      File outputDir = new File(baseDir + pckgname.substring(pckgname.lastIndexOf("java")));
      if (!outputDir.exists()) {
        outputDir.mkdir();
      }
      File file = new File(outputDir + File.separator + cl.getName() + ".jimple");
      PrintWriter writer;
      try {
        writer = new PrintWriter(file);
        Printer printer = new Printer();
        printer.printTo(cl, writer);
        writer.flush();
        writer.close();
      } catch (FileNotFoundException e) {
        // dont throw again - as this is for debug purposes only
        e.printStackTrace();
      }
    }
  }

  public void listJavaSources() {
    // TODO: implement
  }

  public void exportJavaJimple() {
    // TODO: implement to keep Jimple in sync with sourcecode
  }

  public void exportJavaBytecode() {
    // TODO: implement later to keep Bytecode in sync with sourcecode
  }

  @Before
  public void setup() {
    Set<String> locationSet =
        new HashSet(
            Arrays.asList(
                baseDir + "java6", baseDir + "java7", baseDir + "java8"
                // baseDir + "java9",baseDir + "java10"
                ));

    Project project =
        JavaProject.builder(new JavaLanguage(8))
            .addClassPath(new JimpleAnalysisInputLocation(locationSet))
            .build();
    view = project.createOnDemandView();
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
    // FIXME
    return identifierFactory.getClassType(getClassName("THE-FILE-PATH"));
  }

  public SootClass loadClass(ClassType clazz) {

    Optional<SootClass> cs = (Optional<SootClass>) view.getClass(clazz);
    assertTrue("no matching class signature found", cs.isPresent());
    return cs.get();
  }

  public SootMethod loadMethod(MethodSignature methodSignature) {
    SootClass clazz = loadClass(methodSignature.getDeclClassType());
    Optional<SootMethod> m = clazz.getMethod(methodSignature);
    assertTrue("No matching method signature found", m.isPresent());
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
