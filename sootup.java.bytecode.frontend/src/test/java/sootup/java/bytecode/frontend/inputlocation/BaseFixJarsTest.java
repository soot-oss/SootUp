package sootup.java.bytecode.frontend.inputlocation;

import java.util.Collections;
import java.util.stream.Collectors;
import sootup.interceptors.BytecodeBodyInterceptors;
import sootup.java.core.JavaSootClass;
import sootup.java.core.JavaSootMethod;
import sootup.java.core.views.JavaView;

public abstract class BaseFixJarsTest {

  String failedMethodSignature = "";

  public JavaView supplyJavaView(String jarDownloadUrl) {
    DownloadJarAnalysisInputLocation inputLocation =
        new DownloadJarAnalysisInputLocation(
            jarDownloadUrl,
            BytecodeBodyInterceptors.Default.getBodyInterceptors(),
            Collections.emptyList());
    return new JavaView(inputLocation);
  }

  public void assertMethodConversion(JavaView javaView, String methodSignature) {
    try {
      javaView
          .getMethod(javaView.getIdentifierFactory().parseMethodSignature(methodSignature))
          .get()
          .getBody();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void assertJar(JavaView javaView) {
    for (JavaSootClass clazz : javaView.getClasses().collect(Collectors.toList())) {
      for (JavaSootMethod javaSootMethod : clazz.getMethods()) {
        if (javaSootMethod.hasBody()) {
          try {
            javaSootMethod.getBody();
          } catch (Exception exception) {
            failedMethodSignature = javaSootMethod.getSignature().toString();
            throw exception;
          }
        }
      }
    }
  }
}
