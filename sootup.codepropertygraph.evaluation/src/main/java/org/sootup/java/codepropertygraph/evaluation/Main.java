package org.sootup.java.codepropertygraph.evaluation;

import io.shiftleft.codepropertygraph.generated.nodes.Method;
import io.shiftleft.semanticcpg.dotgenerator.DotSerializer.Graph;
import java.util.Optional;
import org.sootup.java.codepropertygraph.evaluation.joern.JoernCfgGenerator;
import org.sootup.java.codepropertygraph.evaluation.sootup.SootUpCfgGenerator;
import sootup.core.model.SootMethod;
import sootup.java.codepropertygraph.MethodInfo;
import sootup.java.codepropertygraph.cfg.CfgCreator;
import sootup.java.codepropertygraph.propertygraph.PropertyGraph;

public class Main {
  public static void main(String[] args) {
    String sourceCodeDirPath = "sootup.codepropertygraph.evaluation/src/test/resources/basicTS";
    String cpgPath = "sootup.codepropertygraph.evaluation/src/test/resources/out.cpg";

    SootUpCfgGenerator sootUpCfgGenerator = new SootUpCfgGenerator(sourceCodeDirPath);
    JoernCfgGenerator joernCfgGenerator = new JoernCfgGenerator(cpgPath);

    PropertyGraphComparer comparer =
        new PropertyGraphComparer(joernCfgGenerator, sootUpCfgGenerator);

    for (SootMethod sootupMethod : sootUpCfgGenerator.getMethods()) {
      String methodSignatureAsJoern = sootUpCfgGenerator.getMethodSignatureAsJoern(sootupMethod);
      Optional<Method> joernMethod = joernCfgGenerator.getMethod(methodSignatureAsJoern);

      Graph joernCfg =
          joernCfgGenerator.getCfg(
              joernMethod.orElseThrow(
                  () ->
                      new RuntimeException(
                          "Joern method was not found: " + methodSignatureAsJoern)));
      PropertyGraph sootUpCfg = CfgCreator.convert(new MethodInfo(sootupMethod));

      Graph joernAst =
              joernCfgGenerator.getAst(
                      joernMethod.orElseThrow(
                              () ->
                                      new RuntimeException(
                                              "Joern method was not found: " + methodSignatureAsJoern)));

      System.out.println(comparer.compareCfg(joernCfg, joernAst, sootUpCfg));
      //break;
    }
  }
}
