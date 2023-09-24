package sootup.examples.bodyInterceptor;

import static org.junit.Assert.assertTrue;

import categories.Java8Test;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.inputlocation.ClassLoadingOptions;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.java.bytecode.inputlocation.PathBasedAnalysisInputLocation;
import sootup.java.bytecode.interceptors.DeadAssignmentEliminator;
import sootup.java.core.JavaProject;
import sootup.java.core.JavaSootClass;
import sootup.java.core.JavaSootClassSource;
import sootup.java.core.language.JavaLanguage;
import sootup.java.core.views.JavaView;

/** This example illustrates how to invoke body interceptors. */
@Category(Java8Test.class)
public class BodyInterceptor {

  @Test
  public void test() {
    // Create a AnalysisInputLocation, which points to a directory. All class files will be loaded
    // from the directory
    AnalysisInputLocation<JavaSootClass> inputLocation =
        PathBasedAnalysisInputLocation.create(
            Paths.get("src/test/resources/BodyInterceptor/binary"), null);

    // Specify the language of the JavaProject. This is especially relevant for Multi-release jars,
    // where classes are loaded depending on the language level of the analysis
    JavaLanguage language = new JavaLanguage(8);

    // Create a new JavaProject based on the input location
    JavaProject project = JavaProject.builder(language).addInputLocation(inputLocation).build();

    // Create a signature for the class we want to analyze
    ClassType classType = project.getIdentifierFactory().getClassType("File");

    // Create a signature for the method we want to analyze
    MethodSignature methodSignature =
        project
            .getIdentifierFactory()
            .getMethodSignature(classType, "someMethod", "void", Collections.emptyList());

    // Create a view for project, which allows us to retrieve classes
    // add class loading options, which can specify body interceptors
    JavaView view = project.createView();
    view.configBodyInterceptors(
        analysisInputLocation ->
            new ClassLoadingOptions() {
              @Nonnull
              @Override
              public List<sootup.core.transform.BodyInterceptor> getBodyInterceptors() {
                return Collections.singletonList(new DeadAssignmentEliminator());
              }
            });

    // Assert that class is present
    assertTrue(view.getClass(classType).isPresent());

    // Retrieve class
    SootClass<JavaSootClassSource> sootClass = view.getClass(classType).get();

    // Retrieve method
    assertTrue(view.getMethod(methodSignature).isPresent());
    SootMethod method = view.getMethod(methodSignature).get();

    System.out.println(method.getBody());

    // assert that l1 = 3 is not present, i.e. body interceptor worked
    assertTrue(
        method.getBody().getStmts().stream()
            .noneMatch(
                stmt ->
                    stmt instanceof JAssignStmt
                        && ((JAssignStmt) stmt).getRightOp().equivTo(IntConstant.getInstance(3))));
  }
}
