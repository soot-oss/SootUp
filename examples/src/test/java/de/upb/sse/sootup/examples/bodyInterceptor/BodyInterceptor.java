package de.upb.sse.sootup.examples.bodyInterceptor;

import static org.junit.Assert.assertTrue;

import categories.Java8Test;
import de.upb.sse.sootup.core.inputlocation.AnalysisInputLocation;
import de.upb.sse.sootup.core.inputlocation.ClassLoadingOptions;
import de.upb.sse.sootup.core.jimple.common.constant.IntConstant;
import de.upb.sse.sootup.core.jimple.common.stmt.JAssignStmt;
import de.upb.sse.sootup.core.model.SootClass;
import de.upb.sse.sootup.core.model.SootMethod;
import de.upb.sse.sootup.core.signatures.MethodSignature;
import de.upb.sse.sootup.core.types.ClassType;
import de.upb.sse.sootup.java.bytecode.inputlocation.PathBasedAnalysisInputLocation;
import de.upb.sse.sootup.java.bytecode.interceptors.DeadAssignmentEliminator;
import de.upb.sse.sootup.java.core.JavaProject;
import de.upb.sse.sootup.java.core.JavaSootClass;
import de.upb.sse.sootup.java.core.JavaSootClassSource;
import de.upb.sse.sootup.java.core.language.JavaLanguage;
import de.upb.sse.sootup.java.core.views.JavaView;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/** This example illustrates how to invoke body interceptors. */
@Category(Java8Test.class)
public class BodyInterceptor {

  @Test
  public void test() {
    // Create a AnalysisInputLocation, which points to a directory. All class files will be loaded
    // from the directory
    AnalysisInputLocation<JavaSootClass> inputLocation =
        new PathBasedAnalysisInputLocation(
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
    JavaView view =
        project.createView(
            analysisInputLocation ->
                new ClassLoadingOptions() {
                  @Nonnull
                  @Override
                  public List<de.upb.sse.sootup.core.transform.BodyInterceptor>
                      getBodyInterceptors() {
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
