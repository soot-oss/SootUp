package sootup.examples.bodyInterceptor;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.java.bytecode.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.interceptors.DeadAssignmentEliminator;
import sootup.java.core.views.JavaView;

/** This example illustrates how to invoke body interceptors. */
@Tag("Java8")
public class BodyInterceptor {

  @Test
  public void test() {
    // Create a AnalysisInputLocation, which points to a directory. All class files will be loaded
    // from the directory
    AnalysisInputLocation inputLocation =
        new JavaClassPathAnalysisInputLocation(
            "src/test/resources/BodyInterceptor/binary",
            null,
            Collections.singletonList(new DeadAssignmentEliminator()));

    // Create a new JavaView based on the input location
    JavaView view = new JavaView(inputLocation);

    // Create a signature for the class we want to analyze
    ClassType classType = view.getIdentifierFactory().getClassType("File");

    // Create a signature for the method we want to analyze
    MethodSignature methodSignature =
        view.getIdentifierFactory()
            .getMethodSignature(classType, "someMethod", "void", Collections.emptyList());

    // Assert that class is present
    assertTrue(view.getClass(classType).isPresent());

    // Retrieve class
    SootClass sootClass = view.getClass(classType).get();

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
