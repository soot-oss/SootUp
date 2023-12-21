package sootup.java.bytecode;

import categories.Java8Test;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.model.SootMethod;
import sootup.java.bytecode.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.JavaSootClass;
import sootup.java.core.views.JavaView;

@Category(Java8Test.class)
public class Soot1577Test {
  final String directory = "../shared-test-resources/soot-1577/";

  @Test
  @Ignore("conversion fails - could be a dex2jar conversion problem")
  public void test() {
    AnalysisInputLocation inputLocation =
        new JavaClassPathAnalysisInputLocation(directory);

    JavaView view = new JavaView(inputLocation);

    Assert.assertEquals(1, view.getClasses().size());

    view.getClasses().stream().findFirst().get().getMethods().forEach(SootMethod::getBody);
  }
}
