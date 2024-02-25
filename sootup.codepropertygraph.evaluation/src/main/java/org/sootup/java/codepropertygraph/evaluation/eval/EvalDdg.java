package org.sootup.java.codepropertygraph.evaluation.eval;

import java.io.IOException;
import org.sootup.java.codepropertygraph.evaluation.graph.generation.DdgGraphGenerator;

public class EvalDdg {
  public static void main(String[] args) throws IOException {
    String resourcesDir = "sootup.codepropertygraph.evaluation/src/test/resources/";
    EvaluatorConfig config =
        new EvaluatorConfig(
            resourcesDir + "joern-artifacts",
            resourcesDir + "sootup-artifacts",
            resourcesDir + "result-logs-ddg",
            resourcesDir + "main_out_ddg.txt");

    PropertyGraphEvaluator evaluator =
        new PropertyGraphEvaluator(new DdgGraphGenerator(), config);
    evaluator.performEvaluation();
  }
}
