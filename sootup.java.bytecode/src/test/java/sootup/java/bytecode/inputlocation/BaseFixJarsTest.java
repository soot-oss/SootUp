package sootup.java.bytecode.inputlocation;

import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.model.SootMethod;
import sootup.java.core.views.JavaView;

public abstract class BaseFixJarsTest {
  String failedJarsPath = "../failed_jars";


  public String getJarPath(String jarName) {
    return failedJarsPath + "/" + jarName;
  }

  public JavaView supplyJavaView(String jarName) {
    AnalysisInputLocation inputLocation =
        new JavaClassPathAnalysisInputLocation(getJarPath(jarName));
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
    try {
      javaView.getClasses()
              .flatMap(clazz -> clazz.getMethods().stream())
              .filter(SootMethod::hasBody)
              .forEach(SootMethod::getBody);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
