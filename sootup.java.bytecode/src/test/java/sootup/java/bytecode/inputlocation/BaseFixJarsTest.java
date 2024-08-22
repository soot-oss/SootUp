package sootup.java.bytecode.inputlocation;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.java.core.views.JavaView;

public abstract class BaseFixJarsTest {
  String failedJarsPath = "../failed_jars";
  String failedJarsInfo = "../failed_jars/jar_failure.csv";

  List<JarFailureRecord> records = new ArrayList<>();

  public String getJarPath(String jarName) {
    return failedJarsPath + "/" + jarName;
  }

  public List<JarFailureRecord> getRecords() {
    if (!Files.exists(Paths.get(failedJarsPath))) {
      System.out.println("File Path does not exists");
      return Collections.emptyList();
    }
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

  public JavaView supplyJavaView(String jarName) {
    AnalysisInputLocation inputLocation =
        new JavaClassPathAnalysisInputLocation(getJarPath(jarName));
    return new JavaView(inputLocation);
  }

  public void assertMethodConversion(String methodSignature, String jarName) {
    try {
      JavaView javaView = supplyJavaView(jarName);
      javaView
          .getMethod(javaView.getIdentifierFactory().parseMethodSignature(methodSignature))
          .get()
          .getBody();
    } catch (Exception e) {
      e.printStackTrace();
    }
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
