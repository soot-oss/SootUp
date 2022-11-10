package de.upb.sse.sootup.examples.basicSetup;

import static org.junit.Assert.assertTrue;

import categories.Java8Test;
import de.upb.sse.sootup.core.Language;
import de.upb.sse.sootup.core.Project;
import de.upb.sse.sootup.core.inputlocation.AnalysisInputLocation;
import de.upb.sse.sootup.core.jimple.common.expr.JVirtualInvokeExpr;
import de.upb.sse.sootup.core.jimple.common.stmt.JInvokeStmt;
import de.upb.sse.sootup.core.model.SootClass;
import de.upb.sse.sootup.core.model.SootMethod;
import de.upb.sse.sootup.core.signatures.MethodSignature;
import de.upb.sse.sootup.core.types.ClassType;
import de.upb.sse.sootup.core.views.View;
import de.upb.sse.sootup.java.bytecode.inputlocation.PathBasedAnalysisInputLocation;
import de.upb.sse.sootup.java.core.JavaProject;
import de.upb.sse.sootup.java.core.JavaSootClass;
import de.upb.sse.sootup.java.core.JavaSootClassSource;
import de.upb.sse.sootup.java.core.language.JavaJimple;
import de.upb.sse.sootup.java.core.language.JavaLanguage;
import de.upb.sse.sootup.java.sourcecode.inputlocation.JavaSourcePathAnalysisInputLocation;
import de.upb.sse.sootup.jimple.parser.JimpleAnalysisInputLocation;
import de.upb.sse.sootup.jimple.parser.JimpleProject;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/** This example illustrates how to create and use a new Soot Project. */
@Category(Java8Test.class)
public class BasicSetup {

  @Test
  public void createSourceCodeProject() {
    Path pathToSource = Paths.get("src/test/resources/BasicSetup/source");
    AnalysisInputLocation<JavaSootClass> inputLocation =
        new JavaSourcePathAnalysisInputLocation(pathToSource.toString());
    Language language = new JavaLanguage(8);
    Project project =
        JavaProject.builder((JavaLanguage) language).addInputLocation(inputLocation).build();
  }

  @Ignore
  public void createJimpleProject() {
    Path pathToJimple = Paths.get("src/test/resources/BasicSetup/jimple");
    AnalysisInputLocation<JavaSootClass> inputLocation =
        new JimpleAnalysisInputLocation(pathToJimple);
    Project project = new JimpleProject(inputLocation);
  }

  @Test
  public void createByteCodeProject() {
    // Create a AnalysisInputLocation, which points to a directory. All class files will be loaded
    // from the directory
    Path pathToBinary = Paths.get("src/test/resources/BasicSetup/binary");
    AnalysisInputLocation<JavaSootClass> inputLocation =
        new PathBasedAnalysisInputLocation(pathToBinary, null);

    // Specify the language of the JavaProject. This is especially relevant for Multi-release jars,
    // where classes are loaded depending on the language level of the analysis
    Language language = new JavaLanguage(8);

    // Create a new JavaProject based on the input location
    Project project =
        JavaProject.builder((JavaLanguage) language).addInputLocation(inputLocation).build();

    // Create a signature for the class we want to analyze
    ClassType classType = project.getIdentifierFactory().getClassType("HelloWorld");

    // Create a signature for the method we want to analyze
    MethodSignature methodSignature =
        project
            .getIdentifierFactory()
            .getMethodSignature(
                classType, "main", "void", Collections.singletonList("java.lang.String[]"));

    // Create a view for project, which allows us to retrieve classes
    View view = project.createView();

    // Assert that class is present
    assertTrue(view.getClass(classType).isPresent());

    // Retrieve class
    SootClass<JavaSootClassSource> sootClass =
        (SootClass<JavaSootClassSource>) view.getClass(classType).get();

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
