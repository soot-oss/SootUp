package org.sootup.java.codepropertygraph.evaluation.eval;

import java.io.IOException;
import org.sootup.java.codepropertygraph.evaluation.graph.generation.CdgGraphGenerator;

public class EvalCdg {
  public static void main(String[] args) throws IOException {
    String resourcesDir = "sootup.codepropertygraph.evaluation/src/test/resources/";
    EvaluatorConfig config =
        new EvaluatorConfig(
            resourcesDir + "joern-artifacts",
            resourcesDir + "sootup-artifacts",
            resourcesDir + "result-logs-cdg",
            resourcesDir + "main_out_cdg.txt");

    PropertyGraphEvaluator evaluator =
        new PropertyGraphEvaluator(new CdgGraphGenerator(), config);
    evaluator.performEvaluation();
  }
}
