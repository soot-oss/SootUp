package sootup.callgraph;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.java.bytecode.frontend.inputlocation.DefaultRuntimeAnalysisInputLocation;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.views.JavaView;

public class AnalysisApacheAnt {
  public static void main(String[] args) {
    List<AnalysisInputLocation> inputLocation = new ArrayList<>();
    inputLocation.add(new DefaultRuntimeAnalysisInputLocation());
    String libDirPath =
        "C:\\Users\\morit\\Desktop\\BachelorThesis\\Real-World_Applications\\ApacheAnt\\ApacheAnt-1.10.15-FULL\\apache-ant-1.10.15\\lib";
    Path libDir = Paths.get(libDirPath);
    try (Stream<Path> paths = Files.list(libDir)) {
      paths
          .filter(Files::isRegularFile)
          .filter(p -> p.toString().endsWith(".jar"))
          .forEach(
              jarPath -> {
                inputLocation.add(new JavaClassPathAnalysisInputLocation(jarPath.toString()));
              });
    } catch (IOException e) {
      throw new RuntimeException("Could not read library directory", e);
    }
    JavaView view = new JavaView(inputLocation);

    ClassType classType1 = view.getIdentifierFactory().getClassType("org.apache.tools.ant.Main");
    MethodSignature entryMethodSignature1 =
        JavaIdentifierFactory.getInstance()
            .getMethodSignature(
                classType1, "main", "void", Collections.singletonList("java.lang.String[]"));
    MethodSignature entryMethodSignature2 =
        JavaIdentifierFactory.getInstance()
            .getMethodSignature(classType1, "clinit", "void", Collections.emptyList());
    ClassType classType2 = view.getIdentifierFactory().getClassType("jdk.nio.zipfs.ZipFileSystem");
    MethodSignature entryMethodSignature3 =
        JavaIdentifierFactory.getInstance()
            .getMethodSignature(classType2, "finalize", "void", Collections.emptyList());
    ClassType classType3 = view.getIdentifierFactory().getClassType("org.apache.tools.zip.ZipFile");
    MethodSignature entryMethodSignature4 =
        JavaIdentifierFactory.getInstance()
            .getMethodSignature(classType3, "finalize", "void", Collections.emptyList());

    List<MethodSignature> entryPoints = new ArrayList<>();
    entryPoints.add(entryMethodSignature1);
    entryPoints.add(entryMethodSignature2);
    entryPoints.add(entryMethodSignature3);
    entryPoints.add(entryMethodSignature4);
    AbstractCallGraphAlgorithm algo = new ClassHierarchyAnalysisAlgorithm(view);
    CallGraph cg = algo.initialize(entryPoints);

    Path output = Paths.get("apacheAntOutputCHA.txt");
    try (BufferedWriter writer =
        Files.newBufferedWriter(
            output, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
      for (CallGraph.Call call : cg.getCalls()) {
        try {
          writer.write(call.toString() + " " + call.getLineNumber());
          writer.newLine();
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
