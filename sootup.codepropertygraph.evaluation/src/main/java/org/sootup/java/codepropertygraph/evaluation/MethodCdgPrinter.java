package org.sootup.java.codepropertygraph.evaluation;

import io.shiftleft.codepropertygraph.generated.nodes.Method;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import org.sootup.java.codepropertygraph.evaluation.joern.JoernCdgGenerator;
import org.sootup.java.codepropertygraph.evaluation.sootup.SootUpCdgGenerator;
import sootup.core.model.SootMethod;
import sootup.java.codepropertygraph.MethodInfo;
import sootup.java.codepropertygraph.cdg.CdgCreator;

public class MethodCdgPrinter {
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
        Path jarFile = Paths.get(SOOTUP_DIR, baseName + ".jar");

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
        "lombok.launch.PatchFixesHider$PatchFixes.findTypeDeclaration:org.eclipse.jdt.core.dom.AbstractTypeDeclaration(org.eclipse.jdt.core.IType,java.util.List)";

    printSootupCdg(jarFile.toString(), methodSignatureAsJoern);
    // printJoernCdg(binFile.toString(), methodSignatureAsJoern);
  }

  private static void printJoernCdg(String cpgPath, String methodSignatureAsJoern) {
    JoernCdgGenerator joernCdgGenerator = new JoernCdgGenerator(cpgPath);
    Method method = joernCdgGenerator.getMethod(methodSignatureAsJoern).get();

    JoernCdgAdapter adapter = new JoernCdgAdapter(joernCdgGenerator);

    System.out.println(
        (adapter.getCdg(joernCdgGenerator.getCdg(method))).toDotGraph(method.name()));
  }

  private static void printSootupCdg(String sourceCodeDirPath, String methodSignatureAsJoern) {
    SootUpCdgGenerator sootUpCdgGenerator = new SootUpCdgGenerator(sourceCodeDirPath);

    SootMethod sootMethod =
        sootUpCdgGenerator.getMethods().stream()
            .filter(
                sm ->
                    sootUpCdgGenerator.getMethodSignatureAsJoern(sm).equals(methodSignatureAsJoern))
            .findAny()
            .get();

    System.out.println(
        CdgCreator.convert(new MethodInfo(sootMethod)).toDotGraph(sootMethod.getName()));
  }

  private static String extractBaseName(Path path) {
    String fileName = path.getFileName().toString();
    return fileName.substring(0, fileName.lastIndexOf('.'));
  }
}
