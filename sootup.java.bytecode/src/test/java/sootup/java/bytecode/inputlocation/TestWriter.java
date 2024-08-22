package sootup.java.bytecode.inputlocation;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class TestWriter {

  String templateStart =
      "package sootup.java.bytecode.inputlocation;\n"
          + "\n"
          + "import categories.TestCategories;\n"
          + "import org.junit.jupiter.api.Tag;\n"
          + "import org.junit.jupiter.api.Test;\n"
          + "import sootup.java.core.views.JavaView;\n"
          + "\n"
          + "@Tag(TestCategories.JAVA_8_CATEGORY)\n"
          + "public class FixJars extends BaseFixJarsTest {\n\n";

  String TEST_TAG = "@Test\n";

  public String getMethodString(String jarName, String methodSignature) {
    String methodName =
        "public void execute" + jarName.replace(".jar", "").replaceAll("[^a-zA-Z]", "") + "(){\n";
    return TEST_TAG
        + methodName
        + "\tString jarName = \""
        + jarName
        + "\";\n"
        + "    String methodSignature = \""
        + methodSignature
        + "\";\n"
        + "    JavaView javaView = supplyJavaView(jarName);\n"
        + "    assertMethodConversion(javaView,methodSignature);\n"
        + "    assertJar(javaView);\n"
        + "}\n\n";
  }

  String templateEnd = "}";

  public String getTestContent() {
    StringBuilder content = new StringBuilder(templateStart);
    for (JarFailureRecord record : getRecords()) {
      content.append(getMethodString(record.jarName, record.failedMethodSignature));
    }
    content.append(templateEnd);
    return content.toString();
  }

  public void writeTestFile() {
    String folderPath = "src/test/java/sootup/java/bytecode/inputlocation";
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

        // Create a JarFailureRecord object from the line
        JarFailureRecord record = new JarFailureRecord(values[0], values[2]);
        records.add(record);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return records;
  }

  public static class JarFailureRecord {
    private final String jarName;
    private final String failedMethodSignature;

    public JarFailureRecord(String jarName, String failedMethodSignature) {
      this.jarName = jarName;
      this.failedMethodSignature = failedMethodSignature;
    }

    public String getJarName() {
      return jarName;
    }

    public String getFailedMethodSignature() {
      return failedMethodSignature;
    }
  }
}
