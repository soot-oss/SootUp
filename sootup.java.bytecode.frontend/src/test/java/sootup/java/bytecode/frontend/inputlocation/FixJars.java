package sootup.java.bytecode.frontend.inputlocation;

import java.nio.file.Paths;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.model.Body;
import sootup.core.model.SourceType;
import sootup.interceptors.NopEliminator;
import sootup.jimple.frontend.JimpleAnalysisInputLocation;
import sootup.jimple.frontend.JimpleView;

public class FixJars extends BaseFixJarsTest {

  @Test
  /* Isolated testcase from:
   * String jarDownloadUrl = "https://repo1.maven.org/maven2/io/github/adven27/exam-core/2024.0.10/exam-core-2024.0.10.jar";
   * String methodSignature = "<io.github.adven27.concordion.extensions.exam.core.logger.LoggingFormatterExtension$LoggingFormatterListener: void afterExample(org.concordion.api.listener.ExampleEvent)>";
   */
  public void executeexamcorejar() {
    AnalysisInputLocation inputLocation =
        new JimpleAnalysisInputLocation(
            Paths.get("./src/test/resources/"),
            SourceType.Application,
            Collections.singletonList(new NopEliminator()));
    JimpleView jimpleView = new JimpleView(inputLocation);
    jimpleView
        .getClasses()
        .forEach(
            sootClass -> {
              Body body =
                  sootClass.getMethodsByName("nopEliminatorBug").stream()
                      .findFirst()
                      .get()
                      .getBody();
            });
  }
}
