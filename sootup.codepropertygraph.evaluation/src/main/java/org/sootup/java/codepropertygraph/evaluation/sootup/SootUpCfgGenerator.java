package org.sootup.java.codepropertygraph.evaluation.sootup;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.java.bytecode.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.codepropertygraph.MethodInfo;
import sootup.java.codepropertygraph.cfg.CfgCreator;
import sootup.java.codepropertygraph.propertygraph.PropertyGraph;
import sootup.java.core.JavaSootClass;
import sootup.java.core.views.JavaView;

public class SootUpCfgGenerator {

  private static int counter = 0;

  public void generateCFG(String jarFilePath, String outputDirectory) {
    List<AnalysisInputLocation<? extends JavaSootClass>> inputLocations = new ArrayList<>();
    inputLocations.add(new JavaClassPathAnalysisInputLocation(jarFilePath));

    JavaView minimalTsView = new JavaView(inputLocations);

    minimalTsView
        .getClasses()
        .forEach(
            cl ->
                cl.getMethods()
                    .forEach(
                        method -> {
                          /*try {*/
                            PropertyGraph graph =
                                CfgCreator.convert(
                                    new MethodInfo(
                                        minimalTsView.getMethod(method.getSignature()).get()));
                            writeToFile(
                                outputDirectory, graph.toDotGraph("CFG"), method.getName(), "CFG");
                          /*} catch (Exception e) {
                            System.out.println(e.getStackTrace());
                          }*/
                        }));
  }

  private static void writeToFile(
      String outputDirectory, String dotGraph, String methodName, String graphType) {
    System.out.println(methodName);
    File file = new File(String.format("%s/%s_%d.dot", outputDirectory, graphType, counter));
    System.out.println(file.toPath());

    // Create the output folder if it doesn't exist
    File folder = file.getParentFile();
    if (folder != null && !folder.exists()) {
      folder.mkdirs();
    }

    try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
      writer.write(dotGraph);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
