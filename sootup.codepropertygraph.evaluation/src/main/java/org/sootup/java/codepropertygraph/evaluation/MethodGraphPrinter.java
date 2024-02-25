package org.sootup.java.codepropertygraph.evaluation;

import io.shiftleft.codepropertygraph.generated.nodes.Method;
import io.shiftleft.semanticcpg.dotgenerator.DotSerializer.Graph;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import org.sootup.java.codepropertygraph.evaluation.graph.converter.joern2sootup.JoernToSootUpConverter;
import org.sootup.java.codepropertygraph.evaluation.graph.adapters.JoernAdapter;
import org.sootup.java.codepropertygraph.evaluation.graph.processing.JoernProcessor;
import org.sootup.java.codepropertygraph.evaluation.graph.processing.SootUpProcessor;
import sootup.core.model.SootMethod;
import sootup.java.codepropertygraph.MethodInfo;
import sootup.java.codepropertygraph.cdg.CdgCreator;
import sootup.java.codepropertygraph.cfg.CfgCreator;
import sootup.java.codepropertygraph.ddg.DdgCreator;
import sootup.java.codepropertygraph.propertygraph.PropertyGraph;

public class MethodGraphPrinter {
  private static final String JOERN_DIR =
      "sootup.codepropertygraph.evaluation/src/test/resources/joern-artifacts";
  private static final String SOOTUP_DIR =
      "sootup.codepropertygraph.evaluation/src/test/resources/sootup-artifacts";

  public static void main(String[] args) {
    try {
      List<Path> binFiles =
          Files.list(Paths.get(JOERN_DIR))
              .filter(p -> p.toString().endsWith(".bin"))
              .collect(Collectors.toList());

      for (Path binFile : binFiles) {
        String baseName = extractBaseName(binFile);
        Path jarFile = Paths.get(SOOTUP_DIR, baseName);

        if (Files.exists(jarFile)) {
          if (!baseName.startsWith("lombok")) continue;
          processFilePair(binFile, jarFile);
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static void processFilePair(Path binFile, Path dirPath) {

    String methodSignatureAsJoern =
        "lombok.Lombok.checkNotNull:java.lang.Object(java.lang.Object,java.lang.String)";

    printSootupGraph(dirPath.toString(), methodSignatureAsJoern, GraphType.CDG);
    printJoernGraph(binFile.toString(), methodSignatureAsJoern, GraphType.CDG);
  }

  private static void printJoernGraph(
      String cpgPath, String methodSignatureAsJoern, GraphType graphType) {
    JoernProcessor joernProcessor = new JoernProcessor(cpgPath);

    Method method = joernProcessor.getMethod(methodSignatureAsJoern).get();

    JoernToSootUpConverter joernToSootUpConverter = new JoernToSootUpConverter();
    JoernAdapter adapter = new JoernAdapter();

    Graph generatedGraph;
    switch (graphType) {
      case CFG:
        generatedGraph = joernProcessor.generateCfg(method);
        break;
      case CDG:
        generatedGraph = joernProcessor.generateCdg(method);
        break;
      case DDG:
        generatedGraph = joernProcessor.generateDdg(method);
        break;
      default:
        throw new RuntimeException("Unexpected graph type: " + graphType);
    }

    System.out.println(
        (adapter.adapt(joernToSootUpConverter.adapt(generatedGraph))).toDotGraph(method.name()));
  }

  private static void printSootupGraph(
      String sourceCodeDirPath, String methodSignatureAsJoern, GraphType graphType) {
    SootUpProcessor sootUpProcessor = new SootUpProcessor(Paths.get(sourceCodeDirPath));

    SootMethod sootMethod =
        sootUpProcessor.getMethods().stream()
            .filter(
                sm -> sootUpProcessor.getMethodSignatureAsJoern(sm).equals(methodSignatureAsJoern))
            .findAny()
            .get();

    PropertyGraph convertedGraph;

    switch (graphType) {
      case CFG:
        convertedGraph = CfgCreator.convert(new MethodInfo(sootMethod));
        break;
      case CDG:
        convertedGraph = CdgCreator.convert(new MethodInfo(sootMethod));
        break;
      case DDG:
        convertedGraph = DdgCreator.convert(new MethodInfo(sootMethod));
        break;
      default:
        throw new RuntimeException("Unexpected graph type: " + graphType);
    }

    System.out.println(convertedGraph.toDotGraph(sootMethod.getName()));
  }

  private static String extractBaseName(Path path) {
    String fileName = path.getFileName().toString();
    return fileName.substring(0, fileName.lastIndexOf('.'));
  }

  private enum GraphType {
    CFG,
    CDG,
    DDG
  }
}
