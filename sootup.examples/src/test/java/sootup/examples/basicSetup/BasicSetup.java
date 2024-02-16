package sootup.examples.basicSetup;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.jimple.common.expr.JVirtualInvokeExpr;
import sootup.core.jimple.common.stmt.JInvokeStmt;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.core.views.View;
import sootup.java.bytecode.inputlocation.PathBasedAnalysisInputLocation;
import sootup.java.core.language.JavaJimple;
import sootup.java.core.views.JavaView;

/** This example illustrates how to create and use a new Soot Project. */
@Tag("Java8")
public class BasicSetup {

  @Test
  public void createByteCodeProject() {
    // Create a AnalysisInputLocation, which points to a directory. All class files will be loaded
    // from the directory
    Path pathToBinary = Paths.get("src/test/resources/BasicSetup/binary");
    AnalysisInputLocation inputLocation = PathBasedAnalysisInputLocation.create(pathToBinary, null);

    // Create a view for project, which allows us to retrieve classes
    View view = new JavaView(inputLocation);

    // Create a signature for the class we want to analyze
    ClassType classType = view.getIdentifierFactory().getClassType("HelloWorld");

    // Create a signature for the method we want to analyze
    MethodSignature methodSignature =
        view.getIdentifierFactory()
            .getMethodSignature(
                classType, "main", "void", Collections.singletonList("java.lang.String[]"));

    // Assert that class is present
    assertTrue(view.getClass(classType).isPresent());

    // Retrieve class
    SootClass sootClass = view.getClass(classType).get();

    // Retrieve method
    view.getMethod(methodSignature);

    // Alternatively:
    assertTrue(sootClass.getMethod(methodSignature.getSubSignature()).isPresent());
    SootMethod sootMethod = sootClass.getMethod(methodSignature.getSubSignature()).get();

    // Read jimple code of method
    System.out.println(sootMethod.getBody());

    // Assert that Hello world print is present
    assertTrue(
        sootMethod.getBody().getStmts().stream()
            .anyMatch(
                stmt ->
                    stmt instanceof JInvokeStmt
                        && stmt.getInvokeExpr() instanceof JVirtualInvokeExpr
                        && stmt.getInvokeExpr()
                            .getArg(0)
                            .equivTo(JavaJimple.getInstance().newStringConstant("Hello World!"))));
  }
}
