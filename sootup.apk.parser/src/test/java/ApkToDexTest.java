import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import sootup.core.model.ClassModifier;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.core.types.VoidType;
import sootup.java.core.JavaSootClass;
import sootup.java.core.views.JavaView;

@Tag("Java8")
public class ApkToDexTest {

  @Test
  public void loadAllClasses() {
    long startTime = System.currentTimeMillis();
    //    Path path =
    // Paths.get("/Users/palaniappanmuthuraman/Documents/Thesis/Evaluation/Evaluation_TaintBench/apks/droidbench_apks");
    //    Path path =
    //        Paths.get(
    //
    // "/Users/palaniappanmuthuraman/Documents/Thesis/Evaluation/Evaluation_TaintBench/apks/playstore_apks");
    //    Path path =
    // Paths.get("/Users/palaniappanmuthuraman/Documents/Thesis/Evaluation/Evaluation_TaintBench/apks/taintbench_apks");
    //    File dir = new File(path.toString());
    //    File[] files = dir.listFiles((dir1, name) -> name.toLowerCase().endsWith(".apk"));
    List<String> failedApks = new ArrayList<>();
    //    assert files != null;
    //    for (File child : files) {
    //      String name = child.getName();
    //    String name = "zoho-show.apk";
    //    String apk_path = path + "/" + name;
    String apk_path = "resources/FlowSensitivity1.apk";
    SimpleDateFormat dateFormat = new SimpleDateFormat("mm:ss");
    ApkAnalysisInputLocation<SootClass> sootClassApkAnalysisInputLocation =
        new ApkAnalysisInputLocation<>(
            Paths.get(apk_path),
            "",
            DexBodyInterceptors.Default.bodyInterceptors());
    JavaView view = new JavaView(sootClassApkAnalysisInputLocation);
    Collection<JavaSootClass> classes;
    try {
      //      System.out.println("Loading Apk: " + name);
      classes = view.getClasses();
      //
      //       view.getMethod(view.getIdentifierFactory().parseMethodSignature("<com.flurry.sdk.w:
      // int w(android.telephony.SignalStrength)>"));
      //        view.getMethod(view.getIdentifierFactory().parseMethodSignature("<mh0.j:
      // java.lang.String j(int,java.lang.String,java.lang.String)>"));
      classes.forEach(JavaSootClass::getMethods);
      //        writeToCSVFile(name,view.getNumberOfStoredClasses(),(System.currentTimeMillis() -
      // startTime) / 1000);
    } catch (Exception exception) {
      exception.printStackTrace();
      //      failedApks.add(name);
      //      System.out.println(
      //          "Failed to convert the " + name + " which has " + view.getCachedClassesCount());
    }
    //    }
    //    System.out.println(files.length - failedApks.size() + " passed out of " + files.length);
    //    System.out.println(failedApks);
  }

  @Test
  public void testDexClassSource() {
    String apk_path = "resources/FlowSensitivity1.apk";
    ApkAnalysisInputLocation<SootClass> sootClassApkAnalysisInputLocation =
        new ApkAnalysisInputLocation<>(
            Paths.get(apk_path),
            "/Users/palaniappanmuthuraman/Documents/android-platforms",
            Collections.emptyList());
    JavaView view = new JavaView(sootClassApkAnalysisInputLocation);
    String className = "android.support.v7.widget.PopupMenu";
    ClassType classType = view.getIdentifierFactory().getClassType(className);
    HashSet<String> interfaceSet = new HashSet<>();
    interfaceSet.add("android.support.v7.internal.view.menu.MenuBuilder$Callback");
    interfaceSet.add("android.support.v7.internal.view.menu.MenuPresenter$Callback");
    Optional<JavaSootClass> aClass = view.getClass(classType);
    assert aClass.isPresent();
    JavaSootClass javaSootClass = aClass.get();
    // Resolve fields and check the number
    assertEquals(6, javaSootClass.getFields().size());
    Set<? extends ClassType> interfaces = javaSootClass.getInterfaces();
    // Resolve interface and check the size and the interface method names
    assertEquals(2, interfaces.size());
    assertEquals(
        interfaceSet, interfaces.stream().map(ClassType::toString).collect(Collectors.toSet()));
    // Resolve and check the superclass name
    assertEquals(javaSootClass.getSuperclass().get().toString(), "java.lang.Object");
    // Resolve and check the class modifiers
    assertEquals(
        javaSootClass.getModifiers().toArray()[0].toString(), ClassModifier.PUBLIC.toString());
  }

  @Test
  public void loadOneClass() {
    String apk_path = "resources/FlowSensitivity1.apk";
    ApkAnalysisInputLocation<SootClass> sootClassApkAnalysisInputLocation =
        new ApkAnalysisInputLocation<>(
            Paths.get(apk_path),
            "/Users/palaniappanmuthuraman/Documents/android-platforms",
            Collections.emptyList());
    JavaView view = new JavaView(sootClassApkAnalysisInputLocation);
    String className = "android.support.v4.app.FragmentState$1";
    String methodName = "FragmentState$1";
    ClassType classType = view.getIdentifierFactory().getClassType(className);
    assertTrue(view.getClass(classType).isPresent());
    // Retrieve class
    SootClass sootClass = (SootClass) view.getClass(classType).get();
    // write MethodSignature
    MethodSignature methodSignature =
        new MethodSignature(classType, methodName, Collections.emptyList(), VoidType.getInstance());
    // Retrieve method
    assertTrue(sootClass.getMethod(methodSignature.getSubSignature()).isPresent());
    SootMethod sootMethod = sootClass.getMethod(methodSignature.getSubSignature()).get();
  }
}
