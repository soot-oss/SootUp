package sootup.java.bytecode;

import categories.TestCategories;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.model.SootMethod;
import sootup.java.bytecode.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.views.JavaView;

/** InvokeDynamics and the Operand stack.. */
@Tag(TestCategories.JAVA_8_CATEGORY)
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
