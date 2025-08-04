package sootup.java.bytecode.frontend.inputlocation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import sootup.core.model.SootMethod;
import sootup.java.bytecode.frontend.FileUtil;
import sootup.java.core.views.JavaView;

public class DownloadJarInputLocationTest {

  @Test
  public void testDownloadJarsInputLocation() {
    DownloadJarAnalysisInputLocation downloadJarAnalysisInputLocation =
        new DownloadJarAnalysisInputLocation(
            "https://repo1.maven.org/maven2/commons-io/commons-io/2.11.0/commons-io-2.11.0.jar",
            Collections.emptyList(),
            Collections.emptyList());
    String tempDirPath = FileUtil.getTempDirectory().toString();
    String fileName = "commons-io-2.11.0.jar";
    Path path = Paths.get(tempDirPath, fileName);
    assert Files.exists(path);
    JavaView view = new JavaView(downloadJarAnalysisInputLocation);
    view.getClasses()
        .flatMap(javaSootClass -> javaSootClass.getMethods().stream())
        .filter(SootMethod::hasBody)
        .forEach(SootMethod::getBody);
    // Deleting the file after our work is done
    try {
      Files.delete(path);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    assert !Files.exists(path);
  }
}
