import dexpler.DexClassSource;
import org.junit.Test;
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
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.JavaSootClass;
import sootup.java.core.JavaSootClassSource;
import sootup.java.core.OverridingJavaClassSource;
import sootup.java.core.views.JavaView;
import sootup.java.core.views.MutableJavaView;

import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static Util.Util.dottedClassName;
import static Util.Util.isByteCodeClassName;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ApkToDexTest {

  @Test
  public void testApkConversion() {
    MutableJavaView view;
    long startTime = System.currentTimeMillis();
    String apk_path = "resources/FlowSensitivity1.apk";
    ApkAnalysisInputLocation<SootClass<JavaSootClassSource>> sootClassApkAnalysisInputLocation =
        new ApkAnalysisInputLocation<>(
            Paths.get(apk_path), "/Users/palaniappanmuthuraman/Documents/android-platforms", Collections.emptyList());
    view = new MutableJavaView(sootClassApkAnalysisInputLocation);
    JavaIdentifierFactory identifierFactory = JavaIdentifierFactory.getInstance();
    Map<String, EnumSet<ClassModifier>> classNamesList =
        sootClassApkAnalysisInputLocation.classNamesList;
    AtomicInteger successfulconvertedNumber = new AtomicInteger();
    try {
      classNamesList.forEach(
          (className, classModifiers) -> {
            if (isByteCodeClassName(className)) {
              className = dottedClassName(className);
            }
            Optional<? extends AbstractClassSource<JavaSootClass>> classSource =
                sootClassApkAnalysisInputLocation.getClassSource(
                    identifierFactory.getClassType(className), view);
            if (classSource.isPresent()) {
              DexClassSource dexClassSource = (DexClassSource) classSource.get();
              ClassType classType = view.getIdentifierFactory().getClassType(className);
              Set<SootMethod> sootMethods = new HashSet<>(dexClassSource.resolveMethods());
              JavaSootClass sootClass =
                  new JavaSootClass(
                      new OverridingJavaClassSource(
                          new EagerInputLocation<>(),
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
//              view.addClass(sootClass);
              successfulconvertedNumber.getAndIncrement();
            }
          });
    } catch (Exception exception) {
      exception.printStackTrace();
    } finally {
      SimpleDateFormat dateFormat = new SimpleDateFormat("mm:ss");
      System.out.println(
          "Time Taken to load 740 classes: "
              + dateFormat.format(System.currentTimeMillis() - startTime));
    }
  }

  @Test
  public void loadAllClasses() {
    // Check if the folder exists and is a directory
//    if (folder.exists() && folder.isDirectory()) {
//      // List all files and directories in the folder
//      File[] files = folder.listFiles();
//      if (files != null) {
//        for (File file : files) {
//          if (file.isFile()) {
//            long startTime = System.currentTimeMillis();
//            SimpleDateFormat dateFormat = new SimpleDateFormat("mm:ss");
//            ApkAnalysisInputLocation<SootClass<JavaSootClassSource>> sootClassApkAnalysisInputLocation =
//                    new ApkAnalysisInputLocation<>(
//                            Paths.get(file.getAbsolutePath()), "/Users/palaniappanmuthuraman/Documents/android-platforms");
//            JavaProject javaProject =
//                    JavaProject.builder(new JavaLanguage(8))
//                            .addInputLocation(sootClassApkAnalysisInputLocation)
//                            .build();
//            JavaView view = javaProject.createView(new FullCacheProvider<>());
//            view.getClasses();
//            String[] pathComponents = file.getAbsolutePath().split("/");
//
//            // Get the value in the last index
//            String apkName = pathComponents[pathComponents.length - 1];
//            System.out.println(
//                    apkName +" had " + view.getClasses().size() + " and it took "
//                            + dateFormat.format(System.currentTimeMillis() - startTime) + " to jimplify them");
//          }
//        }
//      }
//    }
    String apk_path = "resources/FlowSensitivity1.apk";
    long startTime = System.currentTimeMillis();
    SimpleDateFormat dateFormat = new SimpleDateFormat("mm:ss");
    ApkAnalysisInputLocation<SootClass<JavaSootClassSource>> sootClassApkAnalysisInputLocation =
        new ApkAnalysisInputLocation<>(
            Paths.get(apk_path), "/Users/palaniappanmuthuraman/Documents/android-platforms", Collections.emptyList());
    JavaView view = new JavaView(sootClassApkAnalysisInputLocation);;
    view.getClasses();
    System.out.println(
        "Time Taken to load 740 classes: "
            + dateFormat.format(System.currentTimeMillis() - startTime));
    assertEquals(740, view.getNumberOfStoredClasses());
  }

  @Test
  public void loadOneClass() {
    String apk_path = "resources/FlowSensitivity1.apk";
    ApkAnalysisInputLocation<SootClass<JavaSootClassSource>> sootClassApkAnalysisInputLocation =
        new ApkAnalysisInputLocation<>(
            Paths.get(apk_path), "/Users/palaniappanmuthuraman/Documents/android-platforms", Collections.emptyList());
    JavaView view = new JavaView(sootClassApkAnalysisInputLocation);
    String className = "android.support.v4.app.FragmentState$1";
    String methodName = "FragmentState$1";
    ClassType classType = view.getIdentifierFactory().getClassType(className);
    assertTrue(view.getClass(classType).isPresent());
    // Retrieve class
    SootClass<JavaSootClassSource> sootClass =
        (SootClass<JavaSootClassSource>) view.getClass(classType).get();
    // write MethodSignature
    MethodSignature methodSignature =
        new MethodSignature(classType, methodName, Collections.emptyList(), VoidType.getInstance());
    // Retrieve method
    assertTrue(sootClass.getMethod(methodSignature.getSubSignature()).isPresent());
    SootMethod sootMethod = sootClass.getMethod(methodSignature.getSubSignature()).get();
  }
}
