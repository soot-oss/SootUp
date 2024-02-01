package org.sootup.java.codepropertygraph.evaluation;

import io.shiftleft.codepropertygraph.generated.nodes.Method;
import io.shiftleft.semanticcpg.dotgenerator.DotSerializer.Graph;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Optional;
import org.sootup.java.codepropertygraph.evaluation.joern.JoernCfgGenerator;
import org.sootup.java.codepropertygraph.evaluation.sootup.SootUpCfgGenerator;
import sootup.core.model.SootMethod;
import sootup.java.codepropertygraph.MethodInfo;
import sootup.java.codepropertygraph.cfg.CfgCreator;
import sootup.java.codepropertygraph.propertygraph.PropertyGraph;

public class Main {
  public static void main(String[] args) throws FileNotFoundException {
    long startTime = System.currentTimeMillis();

    // String sourceCodeDirPath = "sootup.codepropertygraph.evaluation/src/test/resources/basicTS";
    // String cpgPath = "sootup.codepropertygraph.evaluation/src/test/resources/out.cpg";

    /*System.setOut(
    new PrintStream(
        new DualPrintStream(
            "sootup.codepropertygraph.evaluation/src/test/resources/main_out.txt")));*/
    System.setOut(
        new PrintStream("sootup.codepropertygraph.evaluation/src/test/resources/main_out.txt"));

    String sourceCodeDirPath =
        "sootup.codepropertygraph.evaluation/src/test/resources/commons-lang3-3.14.0.jar";
    String cpgPath = "sootup.codepropertygraph.evaluation/src/test/resources/commons.bin";

    SootUpCfgGenerator sootUpCfgGenerator = new SootUpCfgGenerator(sourceCodeDirPath);
    JoernCfgGenerator joernCfgGenerator = new JoernCfgGenerator(cpgPath);

    PropertyGraphComparer comparer =
        new PropertyGraphComparer(joernCfgGenerator, sootUpCfgGenerator);

    int sampleSize = Math.min(sootUpCfgGenerator.getMethods().size(), 100);

    // for (SootMethod sootupMethod : sootUpCfgGenerator.getMethods()) {
    for (SootMethod sootupMethod : sootUpCfgGenerator.getMethods().subList(0, sampleSize)) {
      String methodSignatureAsJoern = sootUpCfgGenerator.getMethodSignatureAsJoern(sootupMethod);
      Optional<Method> joernMethodOpt = joernCfgGenerator.getMethod(methodSignatureAsJoern);

      if (!joernMethodOpt.isPresent()) {
        System.out.println("Joern method not found: " + methodSignatureAsJoern);
        continue;
      }
      Method joernMethod = joernMethodOpt.get();

      if (sootupMethod.isAbstract()) continue; // TODO: Handle abstract methods

      // Todo: Check out the methdods that were not found by Joern
      Graph joernCfg = joernCfgGenerator.getCfg(joernMethod);
      PropertyGraph sootUpCfg = CfgCreator.convert(new MethodInfo(sootupMethod));

      Graph joernAst = joernCfgGenerator.getAst(joernMethod);

      // if (!sootupMethod.getName().equals("access$600")) continue;

      // System.out.println(comparer.compareCfg(joernCfg, joernAst, sootUpCfg));
      System.out.println("Method Name               : " + sootupMethod.getName());
      comparer.compareCfg(joernCfg, joernAst, sootUpCfg);
      // break;

    }

    int similarEdgesCount = comparer.getTotalSameEdges();
    int differentEdgesCount = comparer.getTotalDiffEdges();
    int totalEdges = comparer.getTotalSameEdges() + comparer.getTotalDiffEdges();
    double similarityPercentage = ((double) similarEdgesCount / totalEdges) * 100;
    similarityPercentage = Math.round(similarityPercentage * 10000) / 10000.0;

    System.out.println("Total similar Edges     : " + similarEdgesCount);
    System.out.println("Total different Edges   : " + differentEdgesCount);
    System.out.println("Similarity Percentage   : " + similarityPercentage + " %");

    long endTime = System.currentTimeMillis();
    long elapsedTime = endTime - startTime;

    // Convert the elapsed time
    long elapsedHours = elapsedTime / (60 * 60 * 1000);
    long remainder = elapsedTime % (60 * 60 * 1000);
    long elapsedMinutes = remainder / (60 * 1000);
    remainder = remainder % (60 * 1000);
    long elapsedSeconds = remainder / 1000;
    long elapsedMilliseconds = remainder % 1000;

    System.out.printf(
        "%n%n%nElapsed Time: %d hours, %d minutes, %d seconds, %d milliseconds\n",
        elapsedHours, elapsedMinutes, elapsedSeconds, elapsedMilliseconds);
  }
}
