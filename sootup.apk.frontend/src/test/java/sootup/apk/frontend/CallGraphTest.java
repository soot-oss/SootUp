package sootup.apk.frontend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import sootup.callgraph.CallGraph;
import sootup.callgraph.CallGraphAlgorithm;
import sootup.callgraph.ClassHierarchyAnalysisAlgorithm;
import sootup.callgraph.RapidTypeAnalysisAlgorithm;
import sootup.core.signatures.MethodSignature;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.JavaSootClass;
import sootup.java.core.views.JavaView;

public class CallGraphTest {

  public static JavaView view;

  String className = "de.ecspride.MainActivity";
  String methodName = "onCreate";
  List<String> methodParameters = List.of("android.os.Bundle");
  String methodReturnType = "void";

  @BeforeAll
  public static void createView() {
    String apk_path = "resources/FlowSensitivity1.apk";
    ApkAnalysisInputLocation sootClassApkAnalysisInputLocation =
        new ApkAnalysisInputLocation(
            Paths.get(apk_path),
            "resources/platforms",
            DexBodyInterceptors.Default.bodyInterceptors());
    JavaClassPathAnalysisInputLocation classPathAnalysisInputLocation =
        new JavaClassPathAnalysisInputLocation(
            "resources"
                + File.separator
                + "platforms"
                + File.separator
                + "android-"
                + sootClassApkAnalysisInputLocation.getAndroidSDKVersionInfo().getApi_version()
                + File.separator
                + "android.jar");
    view = new JavaView(List.of(sootClassApkAnalysisInputLocation, classPathAnalysisInputLocation));
  }

  @Test
  public void testCHACallGraphAlgorithm() {

    MethodSignature onCreateMethodSignature =
        view.getIdentifierFactory()
            .getMethodSignature(className, methodName, methodReturnType, methodParameters);

    CallGraphAlgorithm cha = new ClassHierarchyAnalysisAlgorithm(view);
    CallGraph cg = cha.initialize(List.of(onCreateMethodSignature));

    assertTrue(cg.containsMethod(onCreateMethodSignature));
    assertEquals(9, cg.callsFrom(onCreateMethodSignature).size());
  }

  @Test
  public void testRTACallGraphAlgorithm() {

    MethodSignature onCreateMethodSignature =
        view.getIdentifierFactory()
            .getMethodSignature(className, methodName, methodReturnType, methodParameters);

    CallGraphAlgorithm rta =
        new RapidTypeAnalysisAlgorithm(
            view, view.getClasses().map(JavaSootClass::getType).collect(Collectors.toSet()));

    CallGraph cg = rta.initialize(List.of(onCreateMethodSignature));

    assertTrue(cg.containsMethod(onCreateMethodSignature));
    assertEquals(9, cg.callsFrom(onCreateMethodSignature).size());
  }
}
