package sootup.java.bytecode.inputlocation;

import categories.TestCategories;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.java.core.JavaSootClass;
import sootup.java.core.views.JavaView;

@Tag(TestCategories.JAVA_8_CATEGORY)
public class RandomJarTest {

  private final String jarPath = System.getProperty("jarPath", "");
  private static final String TEST_METRICS_FILE = "jar_test.csv";
  private static final String FAILURE_METRICS_FILE = "jar_failure.csv";
  private boolean isTestFailure = false;
  private String exception = "No Exceptions :)";
  private String failedMethodSignature = "";

  @Test
  public void testJar() {
    if (jarPath.isEmpty()) {
      return;
    }
    System.out.println("Jar file parameter is: " + jarPath);
    long timeTakenForClasses = 0;
    long numberOfMethods = 0;
    long timeTakenForMethods = 0;
    long numberOfClasses = 0;
    try {
      AnalysisInputLocation inputLocation = new JavaClassPathAnalysisInputLocation(jarPath);
      JavaView view = new JavaView(inputLocation);
      long start = System.currentTimeMillis();
      Collection<JavaSootClass> classes = getClasses(view);
      numberOfClasses = classes.size();
      timeTakenForClasses = System.currentTimeMillis() - start;
      start = System.currentTimeMillis();
      numberOfMethods = getMethods(classes);
      timeTakenForMethods = System.currentTimeMillis() - start;
    } catch (Exception e) {
      exception = e.getMessage();
      isTestFailure = true;
    } finally {
      String jarFileName = jarPath.substring(jarPath.lastIndexOf("/") + 1);
      TestMetrics metrics =
          new TestMetrics(
              jarFileName,
              numberOfClasses,
              numberOfMethods,
              timeTakenForClasses,
              timeTakenForMethods,
              exception);
      writeMetrics(
          metrics, isTestFailure ? FAILURE_METRICS_FILE : TEST_METRICS_FILE, isTestFailure);
    }
  }

  @Test
  public void writeFile() {
    System.out.println("This Test is written");
    new TestWriter().writeTestFile();
  }

  public void writeMetrics(TestMetrics testMetrics, String fileName, boolean isFailure) {
    File file = new File(fileName);
    boolean fileExists = file.exists();

    try (FileWriter fw = new FileWriter(file, true);
        PrintWriter writer = new PrintWriter(fw)) {
      if (!fileExists) {
        writer.println(
            isFailure
                ? "jar_name,exception,failedMethodSignature"
                : "jar_name,number_of_classes,number_of_methods,time_taken_for_classes,time_taken_for_methods,exception");
      }

      if (isFailure) {
        // As the parameters in the method signature have delimiter (,), writer thinks that as a two
        // different values, so wrapping in an escape sequence.
        String escapedFailedMethodSignature = "\"" + failedMethodSignature + "\"";
        writer.println(
            testMetrics.getJar_name()
                + ","
                + testMetrics.getException()
                + ","
                + escapedFailedMethodSignature);
      } else {
        writer.println(
            testMetrics.getJar_name()
                + ","
                + testMetrics.getNumberOfClasses()
                + ","
                + testMetrics.getNumberOfMethods()
                + ","
                + testMetrics.getTimeTakenForClasses()
                + ","
                + testMetrics.getTimeTakenForMethods()
                + ","
                + testMetrics.getException());
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private Collection<JavaSootClass> getClasses(JavaView view) {
    try {
      return view.getClasses().collect(Collectors.toList());
    } catch (Exception e) {
      throw new RuntimeException("Error while getting class list", e);
    }
  }

  private long getMethods(Collection<JavaSootClass> classes) {
    try {
      return classes.stream()
          .mapToLong(
              clazz ->
                  clazz.getMethods().stream()
                      .peek(
                          method -> {
                            try {
                              method.getBody();
                              throw new RuntimeException();
                            } catch (Exception e) {
                              failedMethodSignature = method.getSignature().toString();
                              throw new RuntimeException(e);
                            }
                          })
                      .count())
          .sum();
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException("Error while getting methods list", e);
    }
  }

  public static class TestMetrics {
    String jar_name;
    long numberOfClasses;
    long numberOfMethods;
    long timeTakenForClasses;
    long timeTakenForMethods;
    String exception;

    public TestMetrics(
        String jar_name,
        long number_of_classes,
        long number_of_methods,
        long time_taken_for_classes,
        long time_taken_for_methods,
        String exception) {
      this.jar_name = jar_name;
      this.numberOfClasses = number_of_classes;
      this.numberOfMethods = number_of_methods;
      this.timeTakenForClasses = time_taken_for_classes;
      this.timeTakenForMethods = time_taken_for_methods;
      this.exception = exception;
    }

    String getJar_name() {
      return jar_name;
    }

    long getNumberOfClasses() {
      return numberOfClasses;
    }

    long getNumberOfMethods() {
      return numberOfMethods;
    }

    long getTimeTakenForClasses() {
      return timeTakenForClasses;
    }

    long getTimeTakenForMethods() {
      return timeTakenForMethods;
    }

    String getException() {
      return exception;
    }
  }
}
