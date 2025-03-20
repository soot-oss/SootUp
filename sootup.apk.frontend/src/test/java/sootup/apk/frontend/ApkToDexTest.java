package sootup.apk.frontend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import sootup.core.model.ClassModifier;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.core.types.VoidType;
import sootup.java.core.JavaSootClass;
import sootup.java.core.JavaSootMethod;
import sootup.java.core.views.JavaView;

public class ApkToDexTest {

  @Test
  public void testDexClassSource() {
    String apk_path = "resources/FlowSensitivity1.apk";
    ApkAnalysisInputLocation sootClassApkAnalysisInputLocation =
        new ApkAnalysisInputLocation(
            Paths.get(apk_path), "", DexBodyInterceptors.Default.bodyInterceptors());
    JavaView view = new JavaView(sootClassApkAnalysisInputLocation);
    String className = "android.support.v7.widget.PopupMenu";
    String classNameToTestAnnotations = "android/support/v4/app/FragmentState$1";
    ClassType classType = view.getIdentifierFactory().getClassType(className);
    ClassType annotationClassType =
        view.getIdentifierFactory().getClassType(classNameToTestAnnotations);
    HashSet<String> interfaceSet = new HashSet<>();
    interfaceSet.add("android.support.v7.internal.view.menu.MenuBuilder$Callback");
    interfaceSet.add("android.support.v7.internal.view.menu.MenuPresenter$Callback");
    Optional<JavaSootClass> aClass = view.getClass(classType);
    Optional<JavaSootClass> annotationClass = view.getClass(annotationClassType);
    assert annotationClass.isPresent();
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
  public void loadAnApk() {
    String apk_path = "resources/FlowSensitivity1.apk";
    ApkAnalysisInputLocation sootClassApkAnalysisInputLocation =
        new ApkAnalysisInputLocation(
            Paths.get(apk_path), "", DexBodyInterceptors.Default.bodyInterceptors());
    JavaView view = new JavaView(sootClassApkAnalysisInputLocation);
    List<JavaSootClass> classes;
    classes = view.getClasses().collect(Collectors.toList());
    int methodsSize = 0;
    for (JavaSootClass javaSootClass : classes) {
      Set<JavaSootMethod> methods = javaSootClass.getMethods();
      methodsSize += methods.size();
    }
    // There are a total of 740 classes and 10559 methods present in the given APK
    assertEquals(6220, methodsSize);
    assertEquals(740, classes.size());
  }

  @Test
  public void loadOneClass() {
    String apk_path = "resources/FlowSensitivity1.apk";
    ApkAnalysisInputLocation sootClassApkAnalysisInputLocation =
        new ApkAnalysisInputLocation(
            Paths.get(apk_path), "", DexBodyInterceptors.Default.bodyInterceptors());
    JavaView view = new JavaView(sootClassApkAnalysisInputLocation);
    String className = "android.support.v4.app.FragmentState$1";
    String methodName = "<init>";
    ClassType classType = view.getIdentifierFactory().getClassType(className);
    assertTrue(view.getClass(classType).isPresent());
    // Retrieve class
    SootClass sootClass = view.getClass(classType).get();
    // write MethodSignature
    MethodSignature methodSignature =
        new MethodSignature(classType, methodName, Collections.emptyList(), VoidType.getInstance());
    // Retrieve method
    assertTrue(sootClass.getMethod(methodSignature.getSubSignature()).isPresent());
    SootMethod sootMethod = sootClass.getMethod(methodSignature.getSubSignature()).get();
  }
}
