package org.sootup.java.codepropertygraph.evaluation.eval;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import org.sootup.java.codepropertygraph.evaluation.graph.generation.AbstractGraphGenerator;
import org.sootup.java.codepropertygraph.evaluation.graph.util.FileUtils;
import org.sootup.java.codepropertygraph.evaluation.graph.util.LogUtils;
import org.sootup.java.codepropertygraph.evaluation.output.ResultAggregator;

public class PropertyGraphEvaluator {
  private final AbstractGraphGenerator graphGenerator;
  private final EvaluatorConfig config;

  public PropertyGraphEvaluator(AbstractGraphGenerator graphGenerator, EvaluatorConfig config) {
    this.graphGenerator = graphGenerator;
    this.config = config;
  }

  void performEvaluation() throws IOException {
    Path resultDirPath = Paths.get(config.getResultDir());
    FileUtils.createDirectoryIfNotExists(resultDirPath);
    FileUtils.deleteJsonFilesInDirectory(resultDirPath);

    List<Path> cpgPaths =
        Files.list(Paths.get(config.getJoernDir()))
            .filter(p -> p.toString().endsWith(".bin"))
            .collect(Collectors.toList());

    LogUtils.setFileSysOut(config.getLogFile());

    for (Path cpgPath : cpgPaths) {
      String baseName = FileUtils.extractBaseName(cpgPath);
      Path sootUpPath = Paths.get(config.getSootUpDir(), baseName);

      // if (!baseName.startsWith("lombok")) continue;

      if (Files.exists(sootUpPath) && Files.isDirectory(sootUpPath)) {
        graphGenerator.processFilePair(cpgPath, sootUpPath, resultDirPath);
      }
    }

    ResultAggregator.generateSummaryForDirectory(config.getResultDir());
  }
}
