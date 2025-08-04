package sootup.java.bytecode.frontend;

import org.junit.jupiter.api.Test;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.model.SootMethod;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.views.JavaView;

/** InvokeDynamics and the Operand stack.. */
public class IndyTests {
  final String directory = "../shared-test-resources/bugfixes/";

  @Test
  public void test() {
    AnalysisInputLocation inputLocation = new JavaClassPathAnalysisInputLocation(directory);

    JavaView view = new JavaView(inputLocation);
    view.getClass(view.getIdentifierFactory().getClassType("Indy"))
        .get()
        .getMethods()
        .forEach(SootMethod::getBody);
  }
}
