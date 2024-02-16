package org.sootup.java.codepropertygraph.evaluation;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.sootup.java.codepropertygraph.evaluation.sootup.SootUpCfgGenerator;
import sootup.core.model.SootMethod;
import sootup.java.codepropertygraph.MethodInfo;
import sootup.java.codepropertygraph.cfg.CfgCreator;
import sootup.java.codepropertygraph.propertygraph.PropertyGraph;

public class EvalRuntime {
  private static final String JAR_DIR =
      "sootup.codepropertygraph.evaluation/src/test/resources/sootup-artifacts";

  private static final String RESULT_DIR =
      "sootup.codepropertygraph.evaluation/src/test/resources/result-logs-ddg";
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

  public static void main(String[] args) {
    Path jarDirPath = Paths.get(JAR_DIR);
    Path resultDirPath = Paths.get(RESULT_DIR);

    List<JarEvaluationResult> evaluationResults = new ArrayList<>();
    long totalDurationMillis = 0;

    try (Stream<Path> paths = Files.walk(jarDirPath)) {
      List<String> subdirectories =
          paths.filter(Files::isDirectory).map(Path::toString).collect(Collectors.toList());

      for (String directoryPath : subdirectories) {
        long startTime = System.currentTimeMillis();

        processDirectory(directoryPath);

        long endTime = System.currentTimeMillis();
        long durationMillis = endTime - startTime;
        totalDurationMillis += durationMillis;

        JarEvaluationResult result = new JarEvaluationResult(directoryPath, durationMillis);
        evaluationResults.add(result);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    EvaluationSummary summary = new EvaluationSummary(evaluationResults, totalDurationMillis);
    String json = GSON.toJson(summary);

    try {
      Files.createDirectories(resultDirPath);
      Path jsonFilePath = resultDirPath.resolve("joern_runtime.json");
      Files.write(jsonFilePath, json.getBytes());
      System.out.println("Runtime evaluation report saved to: " + jsonFilePath);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static void processDirectory(String dirPath) {

    System.out.println("Processing: " + dirPath);
    SootUpCfgGenerator sootUpCfgGenerator = new SootUpCfgGenerator(Paths.get(dirPath));



    System.out.printf(
            "Number of methods in %s is %d%n", dirPath, sootUpCfgGenerator.getMethods().size());
    for (SootMethod sootupMethod : sootUpCfgGenerator.getMethods()) {
      if (sootupMethod.isAbstract() || sootupMethod.isNative())
        continue; // Todo: handle abstract and native methods

      PropertyGraph sootupCfg = CfgCreator.convert(new MethodInfo(sootupMethod));
    }
    System.out.println("--");
  }

  private static class JarEvaluationResult {
    String jarFile;
    long durationMillis;

    public JarEvaluationResult(String jarFile, long durationMillis) {
      this.jarFile = jarFile;
      this.durationMillis = durationMillis;
    }

    // Add getters or make fields public as needed
  }

  private static class EvaluationSummary {
    List<JarEvaluationResult> jarEvaluations;
    long totalDurationMillis;

    public EvaluationSummary(List<JarEvaluationResult> jarEvaluations, long totalDurationMillis) {
      this.jarEvaluations = jarEvaluations;
      this.totalDurationMillis = totalDurationMillis;
    }
  }
}
