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
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.java.bytecode.frontend.inputlocation.DefaultRuntimeAnalysisInputLocation;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.views.JavaView;

public class AnalysisGoogleJavaFormat2 {
    public static void main(String[] args) {
        List<AnalysisInputLocation> inputLocation = new ArrayList<>();
        inputLocation.add(new DefaultRuntimeAnalysisInputLocation());
        String jarPath = "C:\\Users\\morit\\Desktop\\BachelorThesis\\AnalysisRawCallGraphs\\GoogleJavaFormat\\google-java-format-1.17.0-all-deps.jar";
        inputLocation.add(new JavaClassPathAnalysisInputLocation(jarPath));
        JavaView view = new JavaView(inputLocation);

        ClassType classType1 = view.getIdentifierFactory().getClassType("com.google.googlejavaformat.java.Main");
        MethodSignature entryMethodSignature1 = view.getIdentifierFactory().getMethodSignature(
                classType1,
                "main",
                "void",
                Collections.singletonList("java.lang.String[]")
        );
        ClassType classType2 = view.getIdentifierFactory().getClassType("HelloThesis");
        MethodSignature entryMethodSignature2 = view.getIdentifierFactory().getMethodSignature(
                classType2,
                "main",
                "void",
                Collections.singletonList("java.lang.String[]")
        );

        List<MethodSignature> entryPoints = new ArrayList<>();
        entryPoints.add(entryMethodSignature1);
        entryPoints.add(entryMethodSignature2);

        System.out.println("Entry Point configured: " + entryPoints);

        AbstractCallGraphAlgorithm algo = new RapidTypeAnalysisAlgorithm(view);
        CallGraph cg = algo.initialize(entryPoints);

        Path output = Paths.get("googleJavaFormatOutputRTA.txt");
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