package sootup.java.bytecode.inputlocation;

import categories.TestCategories;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Set;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.model.SootMethod;
import sootup.java.core.JavaSootClass;
import sootup.java.core.JavaSootMethod;
import sootup.java.core.views.JavaView;

@Tag(TestCategories.JAVA_8_CATEGORY)
public class RandomJarTest {

  private final String jarPath = System.getProperty("jarPath", "");

  private boolean isTestFailure = false;

  @Test
  public void testJar() {
    if (jarPath.isEmpty()) {
      return;
    }
    System.out.println("Jar file parameter is: " + jarPath);
    try {
      AnalysisInputLocation inputLocation = new JavaClassPathAnalysisInputLocation(jarPath);
      JavaView view = new JavaView(inputLocation);
      String exception = "No Exceptions :)";
      Collection<JavaSootClass> classes;
      long time_taken_for_classes = 0;
      long number_of_methods = 0;
      long time_taken_for_methods = 0;
      long number_of_classes = 0;
      try {
        long start = System.currentTimeMillis();
        classes = getClasses(view);
        number_of_classes = classes.size();
        time_taken_for_classes = System.currentTimeMillis() - start;
        start = System.currentTimeMillis();
        number_of_methods = getMethods(classes);
        time_taken_for_methods = System.currentTimeMillis() - start;
        throw new RuntimeException("Test failed");
      } catch (Exception e) {
        exception = e.getMessage();
        isTestFailure = true;
      } finally {
        if (!isTestFailure) {
          writeTestMetrics(
              new TestMetrics(
                  jarPath.substring(jarPath.lastIndexOf("/") + 1),
                  number_of_classes,
                  number_of_methods,
                  time_taken_for_classes,
                  time_taken_for_methods,
                  exception));
        } else {
          writeFailureMetrics(
              new TestMetrics(
                  jarPath.substring(jarPath.lastIndexOf("/") + 1), -1, -1, -1, -1, exception));
        }
      }
    } catch (Exception e) {
      writeTestMetrics(
          new TestMetrics(
              jarPath.substring(jarPath.lastIndexOf("/") + 1),
              -1,
              -1,
              -1,
              -1,
              "Could not create JavaClassPathAnalysisInputLocation"));
    }
  }

  public void writeTestMetrics(TestMetrics testMetrics) {
    String file_name = "jar_test.csv";
    File file = new File(file_name);
    boolean fileExists = file.exists();

    try (FileWriter fw = new FileWriter(file, true); // Append mode
        PrintWriter writer = new PrintWriter(fw)) {

      // Write the header if the file doesn't exist
      if (!fileExists) {
        writer.println(
            "jar_name,number_of_classes,number_of_methods,time_taken_for_classes,time_taken_for_methods,exception");
      }

      // Write each metric to the file
      writer.println(
          testMetrics.getJar_name()
              + ","
              + testMetrics.getNumberOfClasses()
              + ","
              + testMetrics.getNumber_of_methods()
              + ","
              + testMetrics.getTime_taken_for_classes()
              + ","
              + testMetrics.getTime_taken_for_classes()
              + ","
              + testMetrics.getException());

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void writeFailureMetrics(TestMetrics testMetrics) {
    String file_name = "jar_failure.csv";
    File file = new File(file_name);
    boolean fileExists = file.exists();
    try (FileWriter fw = new FileWriter(file, true); // Append mode
        PrintWriter writer = new PrintWriter(fw)) {

      // Write the header if the file doesn't exist
      if (!fileExists) {
        writer.println("jar_name,exception");
      }

      // Write each metric to the file
      writer.println(testMetrics.getJar_name() + "," + testMetrics.getException());

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private Collection<JavaSootClass> getClasses(JavaView view) {
    try {
      return view.getClasses();
    } catch (Exception e) {
      throw new RuntimeException("Error while getting class list", e);
    }
  }

  private long getMethods(Collection<JavaSootClass> classes) {
    try {
      int numberof_methods = 0;
      for (JavaSootClass clazz : classes) {
        Set<JavaSootMethod> methods = clazz.getMethods();
        numberof_methods += methods.size();
        methods.forEach(SootMethod::getBody);
      }
      return numberof_methods;
    } catch (Exception e) {
      throw new RuntimeException("Error while getting class list", e);
    }
  }

  public static class TestMetrics {
    String jar_name;
    long number_of_classes;
    long number_of_methods;
    long time_taken_for_classes;
    long time_taken_for_methods;
    String exception;

    public TestMetrics(
        String jar_name,
        long number_of_classes,
        long number_of_methods,
        long time_taken_for_classes,
        long time_taken_for_methods,
        String exception) {
      this.jar_name = jar_name;
      this.number_of_classes = number_of_classes;
      this.number_of_methods = number_of_methods;
      this.time_taken_for_classes = time_taken_for_classes;
      this.time_taken_for_methods = time_taken_for_methods;
      this.exception = exception;
    }

    String getJar_name() {
      return jar_name;
    }

    long getNumberOfClasses() {
      return number_of_classes;
    }

    long getNumber_of_methods() {
      return number_of_methods;
    }

    long getTime_taken_for_classes() {
      return time_taken_for_classes;
    }

    long getTime_taken_for_methods() {
      return time_taken_for_methods;
    }

    String getException() {
      return exception;
    }
  }
}
