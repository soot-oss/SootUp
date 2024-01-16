package sootup.java.bytecode;

import categories.Java9Test;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.model.SootMethod;
import sootup.java.bytecode.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.views.JavaView;

/** InvokeDynamics and the Operand stack.. */
@Category(Java9Test.class)
public class IndyTests {
  final String directory = "../shared-test-resources/bugfixes/";

  @Test
  public void test() {
    AnalysisInputLocation inputLocation = new JavaClassPathAnalysisInputLocation(directory);

    JavaView view = new JavaView(inputLocation);
    Assert.assertEquals(1, view.getClasses().size());

    view.getClasses().stream().findFirst().get().getMethods().forEach(SootMethod::getBody);
  }
}
