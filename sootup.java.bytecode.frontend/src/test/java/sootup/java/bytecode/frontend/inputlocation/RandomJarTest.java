package sootup.java.bytecode.frontend.inputlocation;

import com.google.gson.Gson;
import java.io.*;
import org.junit.jupiter.api.Test;
import sootup.java.core.views.JavaView;

public class RandomJarTest extends BaseFixJarsTest {

  private final String jarDownloadPath = System.getProperty("jarPath", "");
  private static final String FAILURE_METRICS_FILE = "jar_failure.json";

  @Test
  public void testJar() {
    if (jarDownloadPath.isEmpty()) {
      return;
    }
    try {
      JavaView javaView = supplyJavaView(jarDownloadPath);
      assertJar(javaView);
    } catch (Exception e) {
      String exception = e.getMessage();
      String jarFileName = jarDownloadPath.substring(jarDownloadPath.lastIndexOf("/") + 1);
      TestMetrics metrics =
          new TestMetrics(jarFileName, jarDownloadPath, exception, failedMethodSignature);
      writeMetrics(metrics);
    }
  }

  @Test
  public void writeFile() {
    new TestWriter().writeTestFile();
  }

  public void writeMetrics(TestMetrics testMetrics) {
    File file = new File(FAILURE_METRICS_FILE);
    boolean fileExists = file.exists();

    Gson gson = new Gson().newBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
      if (!fileExists) {
        raf.writeBytes("[\n");
        raf.writeBytes(gson.toJson(testMetrics));
        raf.writeBytes("\n]");
      } else {
        long length = file.length();
        raf.seek(length - 2); // Position before the closing "]\n"

        // Append the new JSON object
        raf.writeBytes(",\n");
        raf.writeBytes(gson.toJson(testMetrics));
        raf.writeBytes("\n]");
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static class TestMetrics {
    String jar_name;
    String exception;
    String download_url;
    String failedMethodSignature;

    public TestMetrics(
        String jar_name, String download_url, String exception, String failedMethodSignature) {
      this.jar_name = jar_name;
      this.download_url = download_url;
      this.exception = exception;
      this.failedMethodSignature = failedMethodSignature;
    }
  }
}
