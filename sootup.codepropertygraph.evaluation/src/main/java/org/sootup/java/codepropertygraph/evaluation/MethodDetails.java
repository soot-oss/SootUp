package org.sootup.java.codepropertygraph.evaluation;

import java.util.ArrayList;
import java.util.List;

public class MethodDetails {
  String className;
  String methodName;

  public String getClassName() {
    return className;
  }

  public String getMethodName() {
    return methodName;
  }

  public String getReturnType() {
    return returnType;
  }

  public List<String> getParameterTypes() {
    return parameterTypes;
  }

  String returnType;
  List<String> parameterTypes = new ArrayList<>();
}
