package sootup.java.bytecode.inputlocation;

import categories.TestCategories;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import sootup.core.model.SootMethod;
import sootup.java.core.views.JavaView;

@Tag(TestCategories.JAVA_8_CATEGORY)
public class DownloadJarInputLocationTest {

  @Test
  public void testDownloadJarsInputLocation() {
    DownloadJarAnalysisInputLocation downloadJarAnalysisInputLocation =
        new DownloadJarAnalysisInputLocation(
            "https://repo1.maven.org/maven2/commons-io/commons-io/2.11.0/commons-io-2.11.0.jar",
            Collections.emptyList(),
            Collections.emptyList());
    String tempDirPath = System.getProperty("java.io.tmpdir");
    String fileName = "commons-io-2.11.0.jar";
    assert tempDirPath != null && !tempDirPath.isEmpty();
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
