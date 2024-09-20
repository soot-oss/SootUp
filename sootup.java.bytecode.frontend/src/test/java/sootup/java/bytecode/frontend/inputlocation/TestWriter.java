package sootup.java.bytecode.frontend.inputlocation;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestWriter {

  String templateStart =
      "package sootup.java.bytecode.frontend.inputlocation;\n"
          + "\n"
          + "import categories.TestCategories;\n"
          + "import org.junit.jupiter.api.Tag;\n"
          + "import org.junit.jupiter.api.Test;\n"
          + "import sootup.java.core.views.JavaView;\n"
          + "\n"
          + "@Tag(TestCategories.JAVA_8_CATEGORY)\n"
          + "public class FixJars extends BaseFixJarsTest {\n\n";

  String TEST_TAG = "@Test\n";

  public String getMethodString(String jarDownloadUrl, String methodSignature) {
    System.out.println("Jar download url: " + jarDownloadUrl + " is the one to be the methodName");
    String methodName =
        "public void execute"
            + jarDownloadUrl
                .substring(jarDownloadUrl.lastIndexOf("/") + 1)
                .replaceAll("[^a-zA-Z]", "")
            + "(){\n";
    return TEST_TAG
        + methodName
        + "\tString jarDownloadUrl = \""
        + jarDownloadUrl
        + "\";\n"
        + "    String methodSignature = \""
        + methodSignature
        + "\";\n"
        + "    JavaView javaView = supplyJavaView(jarDownloadUrl);\n"
        + "    assertMethodConversion(javaView,methodSignature);\n"
        + "    assertJar(javaView);\n"
        + "}\n\n";
  }

  String templateEnd = "}";

  public String getTestContent() {
    StringBuilder content = new StringBuilder(templateStart);
    for (JarFailureRecord record : getRecords()) {
      content.append(getMethodString(record.download_url, record.failedMethodSignature));
    }
    content.append(templateEnd);
    return content.toString();
  }

  public void writeTestFile() {
    String folderPath = "src/test/java/sootup/java/bytecode/frontend/inputlocation";
    String filePath = folderPath + "/" + "FixJars.java";
    String content = getTestContent();
    if (content.isEmpty()) {
      return;
    }
    System.out.println(content);
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
      writer.write(content);
      System.out.println("Content successfully written to " + filePath);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  String failedJarsInfo = "jar_failure.csv";

  public List<JarFailureRecord> getRecords() {
    List<JarFailureRecord> records = new ArrayList<>();
    try (BufferedReader br = new BufferedReader(new FileReader(failedJarsInfo))) {
      String line;
      boolean isFirstLine = true;
      while ((line = br.readLine()) != null) {
        // Skip the header line
        if (isFirstLine) {
          isFirstLine = false;
          continue;
        }

        // Split the line by commas, handling commas inside quotes
        String[] values = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");

        // Remove leading and trailing quotes if necessary
        for (int i = 0; i < values.length; i++) {
          values[i] = values[i].replaceAll("^\"|\"$", "");
        }
        System.out.println("Values in the file: " + Arrays.toString(values) + "\n");
        // Create a JarFailureRecord object from the line
        JarFailureRecord record = new JarFailureRecord(values[2], values[3]);
        records.add(record);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return records;
  }

  public static class JarFailureRecord {
    private final String failedMethodSignature;
    private final String download_url;

    public JarFailureRecord(String failedMethodSignature, String download_url) {
      this.failedMethodSignature = failedMethodSignature;
      this.download_url = download_url;
    }
  }
}
