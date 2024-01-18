package org.sootup.java.codepropertygraph.evaluation.sootup;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.java.bytecode.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.codepropertygraph.MethodInfo;
import sootup.java.codepropertygraph.cfg.CfgCreator;
import sootup.java.codepropertygraph.propertygraph.PropertyGraph;
import sootup.java.core.JavaSootClass;
import sootup.java.core.JavaSootMethod;
import sootup.java.core.views.JavaView;

public class SootUpCfgGenerator {

  private static int counter = 0;

  private static void writeToFile(String outputDirectory, String dotGraph, String graphType) {
    File file =
        new File(String.format("%s/%d-%s.dot", outputDirectory, counter, graphType.toLowerCase()));

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

  public void generateCFG(String jarFilePath, String outputDirectory) {
    List<AnalysisInputLocation<? extends JavaSootClass>> inputLocations = new ArrayList<>();
    inputLocations.add(new JavaClassPathAnalysisInputLocation(jarFilePath));

    JavaView view = new JavaView(inputLocations);

    for (JavaSootClass cl : view.getClasses()) {
      System.out.printf("[%s]%n", cl.getName());
      for (JavaSootMethod method : cl.getMethods()) {
        try {
          PropertyGraph graph =
              CfgCreator.convert(new MethodInfo(view.getMethod(method.getSignature()).get()));
          writeToFile(outputDirectory, graph.toDotGraph("CFG"), "CFG");
        } catch (Exception e) {
          String methodType = method.isAbstract() ? "abstract" : "";
          System.out.printf(
              "Failed for %s method: %s%n",
              methodType, method.getSignature().getSubSignature().getName());
        }
        counter++;
      }
      if (counter > 100) break; // Todo: Remove this stmt
    }

    writeMethodNamesFile(outputDirectory, view);
  }

  private void writeMethodNamesFile(String outputDirectory, JavaView view) {
    File file = new File(String.format("%s/methodNames.json", outputDirectory));

    ArrayList<String> methodNames = new ArrayList<>();
    for (JavaSootClass cl : view.getClasses()) {
      for (JavaSootMethod method : cl.getMethods()) {
        String part1 = cl.getClassSource().getClassType().getFullyQualifiedName();
        String part2 = method.getName();
        String part3 = method.getReturnType().toString();
        String part4 =
            "("
                + method.getParameterTypes().stream()
                    .map(Object::toString)
                    .collect(Collectors.joining(", "))
                + ")";

        String merged = part1 + "." + part2 + ":" + part3 + part4;

        //methodNames.add(method.getSignature().toString());
        methodNames.add(merged);
      }
    }

    // Create the output folder if it doesn't exist
    File folder = file.getParentFile();
    if (folder != null && !folder.exists()) {
      folder.mkdirs();
    }

    Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    try (BufferedWriter writer =
        new BufferedWriter(
            new OutputStreamWriter(Files.newOutputStream(file.toPath()), StandardCharsets.UTF_8))) {
      gson.toJson(methodNames, writer);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
