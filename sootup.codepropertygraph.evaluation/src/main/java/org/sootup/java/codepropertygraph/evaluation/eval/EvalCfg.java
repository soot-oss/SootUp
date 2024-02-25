package org.sootup.java.codepropertygraph.evaluation.eval;

import java.io.IOException;

import org.sootup.java.codepropertygraph.evaluation.graph.generation.CfgGraphGenerator;

public class EvalCfg {
  public static void main(String[] args) throws IOException {
    String resourcesDir = "sootup.codepropertygraph.evaluation/src/test/resources/";
    EvaluatorConfig config =
        new EvaluatorConfig(
            resourcesDir + "joern-artifacts",
            resourcesDir + "sootup-artifacts",
            resourcesDir + "result-logs-cfg",
            resourcesDir + "main_out_cfg.txt");

    PropertyGraphEvaluator evaluator =
        new PropertyGraphEvaluator(new CfgGraphGenerator(), config);
    evaluator.performEvaluation();
  }
}
