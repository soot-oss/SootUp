package de.upb.swt.soot.examples.basicSetup;

import static org.junit.Assert.assertTrue;

import categories.Java8Test;
import de.upb.swt.soot.core.Language;
import de.upb.swt.soot.core.Project;
import de.upb.swt.soot.core.inputlocation.AnalysisInputLocation;
import de.upb.swt.soot.core.jimple.common.expr.JVirtualInvokeExpr;
import de.upb.swt.soot.core.jimple.common.stmt.JInvokeStmt;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.core.views.View;
import de.upb.swt.soot.java.bytecode.inputlocation.PathBasedAnalysisInputLocation;
import de.upb.swt.soot.java.core.JavaProject;
import de.upb.swt.soot.java.core.JavaSootClass;
import de.upb.swt.soot.java.core.JavaSootClassSource;
import de.upb.swt.soot.java.core.language.JavaJimple;
import de.upb.swt.soot.java.core.language.JavaLanguage;
import de.upb.swt.soot.java.sourcecode.inputlocation.JavaSourcePathAnalysisInputLocation;
import de.upb.swt.soot.jimple.parser.JimpleAnalysisInputLocation;
import de.upb.swt.soot.jimple.parser.JimpleProject;
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
        PathBasedAnalysisInputLocation.createForClassContainer(pathToBinary,null);

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
                "main", classType, "void", Collections.singletonList("java.lang.String[]"));

    // Create a view for project, which allows us to retrieve classes
    View view = project.createOnDemandView();

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
