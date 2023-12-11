package sootup.java.bytecode;

import categories.Java9Test;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.model.SootMethod;
import sootup.java.bytecode.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.JavaProject;
import sootup.java.core.JavaSootClass;
import sootup.java.core.language.JavaLanguage;
import sootup.java.core.views.JavaView;

/** InvokeDynamics and the Operand stack.. */
@Category(Java9Test.class)
public class IndyTests {
  final String directory = "../shared-test-resources/bugfixes/";

  @Test
  public void test() {
    AnalysisInputLocation<JavaSootClass> inputLocation =
        new JavaClassPathAnalysisInputLocation(directory);

    JavaProject project =
        JavaProject.builder(new JavaLanguage(8)).addInputLocation(inputLocation).build();

    JavaView view = project.createView();
    view.getClass(view.getIdentifierFactory().getClassType("Indy"))
        .get()
        .getMethods()
        .forEach(SootMethod::getBody);
  }
}
