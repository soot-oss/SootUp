package org.sootup.java.codepropertygraph.evaluation;

import org.sootup.java.codepropertygraph.evaluation.joern.JoernCfgGenerator;
import org.sootup.java.codepropertygraph.evaluation.sootup.SootUpCfgGenerator;

public class Main {
  public static void main(String[] args) {
    String jarFilePath = "sootup.codepropertygraph.evaluation/src/main/resources/commons-lang3-3.14.0.jar";
    /*String joernOutputDirectory = "sootup.codepropertygraph.evaluation/src/main/temp/joernOutDir";

    JoernCfgGenerator joernCfgGenerator = new JoernCfgGenerator();
    joernCfgGenerator.generateCFG(jarFilePath, joernOutputDirectory);*/

    String sootUpOutputDirectory = "sootup.codepropertygraph.evaluation/src/main/temp/sootUpOutDir";

    SootUpCfgGenerator sootUpCfgGenerator = new SootUpCfgGenerator();
    sootUpCfgGenerator.generateCFG(jarFilePath, sootUpOutputDirectory);

  }
}
