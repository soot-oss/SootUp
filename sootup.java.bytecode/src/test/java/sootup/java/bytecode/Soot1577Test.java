package sootup.java.bytecode;

import categories.Java8Test;
import java.nio.file.Paths;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.model.SootMethod;
import sootup.core.model.SourceType;
import sootup.java.bytecode.inputlocation.PathBasedAnalysisInputLocation;
import sootup.java.core.views.JavaView;

@Category(Java8Test.class)
public class Soot1577Test {

  @Test
  public void test() {
    AnalysisInputLocation inputLocation =
        new PathBasedAnalysisInputLocation.ClassFileBasedAnalysisInputLocation(
            Paths.get("../shared-test-resources/soot-1577/g.class"),
            "cn.com.chinatelecom.account.api.c",
            SourceType.Application);

    JavaView view = new JavaView(inputLocation);

    Assert.assertEquals(1, view.getClasses().size());

    view.getClasses().stream().findFirst().get().getMethods().forEach(SootMethod::getBody);
  }
}
