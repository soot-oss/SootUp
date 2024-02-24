package org.sootup.java.codepropertygraph.evaluation.eval;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.views.View;
import sootup.java.bytecode.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.codepropertygraph.MethodInfo;
import sootup.java.codepropertygraph.ast.AstCreator;
import sootup.java.codepropertygraph.cdg.CdgCreator;
import sootup.java.codepropertygraph.cfg.CfgCreator;
import sootup.java.codepropertygraph.propertygraph.PropertyGraph;
import sootup.java.core.views.JavaView;

public class EvalRuntime {
  private static final String JAR_DIR =
      "sootup.codepropertygraph.evaluation/src/test/resources/sootup-artifacts";

  private static final String RESULT_DIR =
      "sootup.codepropertygraph.evaluation/src/test/resources/result-logs-runtime";
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

  public static void main(String[] args) {
    Path jarDirPath = Paths.get(JAR_DIR);
    Path resultDirPath = Paths.get(RESULT_DIR);

    List<JarEvaluationResult> evaluationResults = new ArrayList<>();
    long totalDurationMillis = 0;

    try (Stream<Path> paths = Files.walk(jarDirPath)) {
      List<Path> jarFiles =
          paths
              .filter(Files::isRegularFile)
              .filter(path -> path.toString().endsWith(".jar"))
              .collect(Collectors.toList());

      for (Path jarFile : jarFiles) {
        long startTime = System.currentTimeMillis();

        processJarFile(jarFile.toString());

        long endTime = System.currentTimeMillis();
        long durationMillis = endTime - startTime;
        totalDurationMillis += durationMillis;

        JarEvaluationResult result =
            new JarEvaluationResult(jarFile.getFileName().toString(), durationMillis);
        evaluationResults.add(result);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    RuntimeEvaluationSummary summary =
        new RuntimeEvaluationSummary(evaluationResults, totalDurationMillis);
    String json = GSON.toJson(summary);

    try {
      Files.createDirectories(resultDirPath);
      Path jsonFilePath = resultDirPath.resolve("sootup_runtime.json");
      Files.write(jsonFilePath, json.getBytes());
      System.out.println("Runtime evaluation report saved to: " + jsonFilePath);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static void processJarFile(String jarPath) {
    System.out.println("Processing: " + jarPath);
    List<AnalysisInputLocation> inputLocations = new ArrayList<>();
    inputLocations.add(new JavaClassPathAnalysisInputLocation(jarPath));
    View view = new JavaView(inputLocations);

    PropertyGraph ast, cfg, cdg = null;
    for (SootClass cl : view.getClasses()) {
      for (SootMethod method : cl.getMethods()) {
        if (method.isAbstract() || method.isNative()) continue;

        try {
          ast = AstCreator.convert(new MethodInfo(method));
          cfg = CfgCreator.convert(new MethodInfo(method));
          cdg = CdgCreator.convert(new MethodInfo(method));
        } catch (RuntimeException e) {
          e.printStackTrace();
        }
      }
    }
  }

  private static class JarEvaluationResult {
    String jarFile;
    long durationMillis;
    int hours;
    int minutes;
    int seconds;
    int milliseconds;

    public JarEvaluationResult(String jarFile, long durationMillis) {
      this.jarFile = jarFile;
      this.durationMillis = durationMillis;

      this.hours = (int) (durationMillis / (1000 * 60 * 60));
      this.minutes = (int) (durationMillis / (1000 * 60) % 60);
      this.seconds = (int) (durationMillis / 1000 % 60);
      this.milliseconds = (int) (durationMillis % 1000);
    }
  }

  private static class RuntimeEvaluationSummary {
    List<JarEvaluationResult> jarEvaluations;
    long totalDurationMillis;
    int totalHours;
    int totalMinutes;
    int totalSeconds;
    int totalMilliseconds;

    public RuntimeEvaluationSummary(
        List<JarEvaluationResult> jarEvaluations, long totalDurationMillis) {
      this.jarEvaluations = jarEvaluations;
      this.totalDurationMillis = totalDurationMillis;

      // Convert totalDurationMillis into hours, minutes, seconds, and milliseconds
      this.totalHours = (int) (totalDurationMillis / (1000 * 60 * 60));
      this.totalMinutes = (int) (totalDurationMillis / (1000 * 60) % 60);
      this.totalSeconds = (int) (totalDurationMillis / 1000 % 60);
      this.totalMilliseconds = (int) (totalDurationMillis % 1000);
    }
  }
}
