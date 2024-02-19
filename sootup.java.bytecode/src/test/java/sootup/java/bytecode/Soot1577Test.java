package sootup.java.bytecode;

import static org.junit.jupiter.api.Assertions.assertEquals;

import categories.TestCategories;
import java.nio.file.Paths;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.model.SootMethod;
import sootup.core.model.SourceType;
import sootup.java.bytecode.inputlocation.PathBasedAnalysisInputLocation;
import sootup.java.core.views.JavaView;

@Tag(TestCategories.JAVA_8_CATEGORY)
public class Soot1577Test {
  final String directory = "../shared-test-resources/soot-1577/";

  @Test
  @Disabled("conversion fails - could be a dex2jar conversion problem")
  public void test() {
    AnalysisInputLocation inputLocation =
        new PathBasedAnalysisInputLocation.ClassFileBasedAnalysisInputLocation(
            Paths.get("../shared-test-resources/soot-1577/g.class"),
            "cn.com.chinatelecom.account.api.c",
            SourceType.Application);

    JavaView view = new JavaView(inputLocation);

    assertEquals(1, view.getClasses().size());

    view.getClasses().stream().findFirst().get().getMethods().forEach(SootMethod::getBody);
  }
}
