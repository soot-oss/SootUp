import static Util.Util.dottedClassName;
import static Util.Util.isByteCodeClassName;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dexpler.DexClassSource;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import instruction.ReturnInstruction;
import org.junit.jupiter.api.Test;
import sootup.core.frontend.AbstractClassSource;
import sootup.core.inputlocation.EagerInputLocation;
import sootup.core.jimple.basic.NoPositionInformation;
import sootup.core.model.ClassModifier;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.model.SourceType;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.core.types.VoidType;
import sootup.java.core.*;
import sootup.java.core.types.JavaClassType;
import sootup.java.core.views.JavaView;
import sootup.java.core.views.MutableJavaView;

public class ApkToDexTest {

  @Test
  public void testApkConversion() {
    MutableJavaView view;
    long startTime = System.currentTimeMillis();
    Path path = Paths.get("/Users/palaniappanmuthuraman/Desktop/APK's");
    File dir = new File(path.toString());
    File[] files = dir.listFiles((dir1, name) -> name.toLowerCase().endsWith(".apk"));
    assert files != null;
    for (File child : files) {
      String apk_path = child.getAbsolutePath();
      ApkAnalysisInputLocation<SootClass> sootClassApkAnalysisInputLocation =
          new ApkAnalysisInputLocation<>(
              Paths.get(apk_path),
              "/Users/palaniappanmuthuraman/Documents/android-platforms",
              Collections.emptyList());
      view = new MutableJavaView(sootClassApkAnalysisInputLocation);
      JavaIdentifierFactory identifierFactory = JavaIdentifierFactory.getInstance();
      Map<String, EnumSet<ClassModifier>> classNamesList =
          sootClassApkAnalysisInputLocation.classNamesList;
      AtomicInteger successfulconvertedNumber = new AtomicInteger();
      boolean failed = false;
      System.out.println(
          "Started " + child.getName() + " with " + classNamesList.size() + " number of classes.");
      try {
        MutableJavaView finalView = view;
        classNamesList.forEach(
            (className, classModifiers) -> {
              if (isByteCodeClassName(className)) {
                className = dottedClassName(className);
              }
              Optional<? extends AbstractClassSource> classSource =
                  sootClassApkAnalysisInputLocation.getClassSource(
                      identifierFactory.getClassType(className), finalView);
              if (classSource.isPresent()) {
                DexClassSource dexClassSource = (DexClassSource) classSource.get();
                JavaClassType classType = finalView.getIdentifierFactory().getClassType(className);
                Set<JavaSootMethod> sootMethods = new HashSet<>(dexClassSource.resolveMethods());
                JavaSootClass sootClass =
                    new JavaSootClass(
                        new OverridingJavaClassSource(
                            new EagerInputLocation(),
                            null,
                            classType,
                            null,
                            null,
                            null,
                            new LinkedHashSet<>(),
                            sootMethods,
                            NoPositionInformation.getInstance(),
                            EnumSet.of(ClassModifier.PUBLIC),
                            Collections.emptyList(),
                            Collections.emptyList(),
                            Collections.emptyList()),
                        SourceType.Application);
                finalView.addClass(sootClass);
                successfulconvertedNumber.getAndIncrement();
              }
              if (successfulconvertedNumber.get() % 100 == 0) {
                System.out.println(
                    "Successfully converted "
                        + successfulconvertedNumber
                        + " out of"
                        + classNamesList.size()
                        + " classes.");
              }
            });
      } catch (Exception exception) {
        exception.printStackTrace();
        System.out.println(
            "Failed to convert the " + child.getName() + " which has " + classNamesList.size());
        failed = true;
      } finally {
        if (!failed) {
          SimpleDateFormat dateFormat = new SimpleDateFormat("mm:ss");
          System.out.println(
              "Time Taken to load "
                  + child.getName()
                  + " "
                  + successfulconvertedNumber
                  + " out of "
                  + classNamesList.size()
                  + " classes: "
                  + dateFormat.format(System.currentTimeMillis() - startTime));
        }
      }
    }
  }

  @Test
  public void loadAllClasses() {
    long startTime = System.currentTimeMillis();
    //    Path path =
    // Paths.get("/Users/palaniappanmuthuraman/Documents/Thesis/Evaluation/Evaluation_TaintBench/apks/droidbench_apks");
    Path path =
        Paths.get(
            "/Users/palaniappanmuthuraman/Documents/Thesis/Evaluation/Evaluation_TaintBench/apks/playstore_apks");
    //    Path path =
    // Paths.get("/Users/palaniappanmuthuraman/Documents/Thesis/Evaluation/Evaluation_TaintBench/apks/taintbench_apks");
    File dir = new File(path.toString());
    File[] files = dir.listFiles((dir1, name) -> name.toLowerCase().endsWith(".apk"));
    List<String> failedApks = new ArrayList<>();
    assert files != null;
    //    for (File child : files) {
    //      String name = child.getName();
    String name = "zoho-show.apk";
    String apk_path = path + "/" + name;
    SimpleDateFormat dateFormat = new SimpleDateFormat("mm:ss");
    ApkAnalysisInputLocation<SootClass> sootClassApkAnalysisInputLocation =
        new ApkAnalysisInputLocation<>(
            Paths.get(apk_path),
            "/Users/palaniappanmuthuraman/Documents/android-platforms",
            DexBodyInterceptors.Default.bodyInterceptors());
    JavaView view = new JavaView(sootClassApkAnalysisInputLocation);
    Collection<JavaSootClass> classes;
    try {
      System.out.println("Loading Apk: " + name);
      classes = view.getClasses();
      //
       view.getMethod(view.getIdentifierFactory().parseMethodSignature("<com.flurry.sdk.w: int w(android.telephony.SignalStrength)>"));
      //        view.getMethod(view.getIdentifierFactory().parseMethodSignature("<mh0.j:
      // java.lang.String j(int,java.lang.String,java.lang.String)>"));
      classes.forEach(JavaSootClass::getMethods);
      //        writeToCSVFile(name,view.getNumberOfStoredClasses(),(System.currentTimeMillis() -
      // startTime) / 1000);
    } catch (Exception exception) {
      exception.printStackTrace();
      failedApks.add(name);
      System.out.println(
          "Failed to convert the " + name + " which has " + view.getCachedClassesCount());
    }
    //    }
    System.out.println(files.length - failedApks.size() + " passed out of " + files.length);
    System.out.println(failedApks);
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
