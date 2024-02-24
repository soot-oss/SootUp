package org.sootup.java.codepropertygraph.evaluation.graph.model;

import java.util.List;

public class JoernMethodDetails {
  String className;
  String methodName;
  String returnType;
  List<String> parameterTypes;

  public JoernMethodDetails(
      String className, String methodName, List<String> parameterTypes, String returnType) {
    this.className = className;
    this.methodName = methodName;
    this.parameterTypes = parameterTypes;
    this.returnType = returnType;
  }

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
}
