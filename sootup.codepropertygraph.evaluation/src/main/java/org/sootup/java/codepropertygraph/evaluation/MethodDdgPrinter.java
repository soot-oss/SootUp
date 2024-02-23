package org.sootup.java.codepropertygraph.evaluation;

import io.shiftleft.codepropertygraph.generated.nodes.Method;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import org.sootup.java.codepropertygraph.evaluation.joern.JoernDdgGenerator;
import org.sootup.java.codepropertygraph.evaluation.sootup.SootUpDdgGenerator;
import sootup.core.model.SootMethod;
import sootup.java.codepropertygraph.MethodInfo;
import sootup.java.codepropertygraph.ddg.DdgCreator;

public class MethodDdgPrinter {
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

  private static void processFilePair(Path binFile, Path jarFile) {
    String methodSignatureAsJoern =
        "lombok.launch.PatchFixesHider$Tests.getBundle:java.lang.Object(java.lang.Object,java.lang.Class)";

    // printSootupDdg(jarFile.toString(), methodSignatureAsJoern);
    printJoernDdg(binFile.toString(), methodSignatureAsJoern);
  }

  private static void printJoernDdg(String cpgPath, String methodSignatureAsJoern) {
    JoernDdgGenerator joernDdgGenerator = new JoernDdgGenerator(cpgPath);
    Method method = joernDdgGenerator.getMethod(methodSignatureAsJoern).get();

    JoernDdgAdapter adapter = new JoernDdgAdapter(joernDdgGenerator);

    System.out.println(
        (adapter.getDdg(joernDdgGenerator.getDdg(method))).toDotGraph(method.name()));
  }

  private static void printSootupDdg(String sourceCodeDirPath, String methodSignatureAsJoern) {
    SootUpDdgGenerator sootUpDdgGenerator = new SootUpDdgGenerator(Paths.get(sourceCodeDirPath));

    SootMethod sootMethod =
        sootUpDdgGenerator.getMethods().stream()
            .filter(
                sm ->
                    sootUpDdgGenerator.getMethodSignatureAsJoern(sm).equals(methodSignatureAsJoern))
            .findAny()
            .get();

    System.out.println(
        DdgCreator.convert(new MethodInfo(sootMethod)).toDotGraph(sootMethod.getName()));
  }

  private static String extractBaseName(Path path) {
    String fileName = path.getFileName().toString();
    return fileName.substring(0, fileName.lastIndexOf('.'));
  }
}
