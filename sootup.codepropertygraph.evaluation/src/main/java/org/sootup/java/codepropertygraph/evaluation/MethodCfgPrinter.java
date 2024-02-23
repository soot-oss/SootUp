package org.sootup.java.codepropertygraph.evaluation;

import io.shiftleft.codepropertygraph.generated.nodes.Method;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import org.sootup.java.codepropertygraph.evaluation.joern.JoernCfgGenerator;
import org.sootup.java.codepropertygraph.evaluation.sootup.SootUpCfgGenerator;
import sootup.core.model.SootMethod;
import sootup.java.codepropertygraph.MethodInfo;
import sootup.java.codepropertygraph.cfg.CfgCreator;

public class MethodCfgPrinter {
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
        // "lombok.javac.apt.Processor.collectData:java.lang.String(javax.annotation.processing.ProcessingEnvironment)";
        // "lombok.launch.ShadowClassLoader.getOrMakeJarListing:java.util.Set(java.lang.String)";
        // "lombok.launch.PatchFixesHider$PatchFixes.findTypeDeclaration:org.eclipse.jdt.core.dom.AbstractTypeDeclaration(org.eclipse.jdt.core.IType,java.util.List)";
        // "lombok.launch.Main.main:void(java.lang.String[])";
        "lombok.launch.PatchFixesHider$Tests.getBundle:java.lang.Object(java.lang.Object,java.lang.Class)";

    // printSootupCfg(dirPath.toString(), methodSignatureAsJoern);
    printJoernCfg(binFile.toString(), methodSignatureAsJoern);
  }

  private static void printJoernCfg(String cpgPath, String methodSignatureAsJoern) {
    JoernCfgGenerator joernCfgGenerator = new JoernCfgGenerator(cpgPath);

    Method method = joernCfgGenerator.getMethod(methodSignatureAsJoern).get();

    JoernCfgAdapter adapter = new JoernCfgAdapter(joernCfgGenerator);

    System.out.println(
        (adapter.getCfg(joernCfgGenerator.getCfg(method))).toDotGraph(method.name()));
  }

  private static void printSootupCfg(String sourceCodeDirPath, String methodSignatureAsJoern) {
    SootUpCfgGenerator sootUpCfgGenerator = new SootUpCfgGenerator(Paths.get(sourceCodeDirPath));

    SootMethod sootMethod =
        sootUpCfgGenerator.getMethods().stream()
            .filter(
                sm ->
                    sootUpCfgGenerator.getMethodSignatureAsJoern(sm).equals(methodSignatureAsJoern))
            .findAny()
            .get();

    System.out.println(
        CfgCreator.convert(new MethodInfo(sootMethod)).toDotGraph(sootMethod.getName()));
  }

  private static String extractBaseName(Path path) {
    String fileName = path.getFileName().toString();
    return fileName.substring(0, fileName.lastIndexOf('.'));
  }
}
