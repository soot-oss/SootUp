package sootup.java.bytecode.frontend;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Paths;
import org.junit.jupiter.api.Test;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.model.SootMethod;
import sootup.core.model.SourceType;
import sootup.java.bytecode.frontend.inputlocation.ClassFileBasedAnalysisInputLocation;
import sootup.java.core.views.JavaView;

public class Soot1577Test {
  final String directory = "../shared-test-resources/soot-1577/";

  @Test
  public void test() {
    AnalysisInputLocation inputLocation =
        new ClassFileBasedAnalysisInputLocation(
            Paths.get("../shared-test-resources/soot-1577/g.class"),
            "cn.com.chinatelecom.account.api.c",
            SourceType.Application);

    JavaView view = new JavaView(inputLocation);

    assertEquals(1, view.getClasses().count());

    view.getClasses().findFirst().get().getMethods().forEach(SootMethod::getBody);
  }
}
