package org.sootup.java.codepropertygraph.evaluation;

import java.util.ArrayList;
import java.util.List;

public class MethodSignatureParser {
  public static boolean isDynamicInvoke(String signature) {
    return parseMethodSignature(signature).methodName.equals("bootstrap$");
  }

  public static MethodDetails parseMethodSignature(String signature) {
    MethodDetails details = new MethodDetails();

    // Split the signature by ':' to separate the class.method from return type and parameters
    String[] parts = signature.split(":");

    if (parts.length == 2) {
      // Extract class name and method name
      int lastDotIndex = parts[0].lastIndexOf('.');
      details.className = parts[0].substring(0, lastDotIndex);
      details.methodName = parts[0].substring(lastDotIndex + 1);

      // Extract return type
      String returnTypeAndParams = parts[1];
      int paramsStartIndex = returnTypeAndParams.indexOf('(');
      details.returnType = returnTypeAndParams.substring(0, paramsStartIndex);

      // Extract parameter types
      String paramsSection =
          returnTypeAndParams.substring(paramsStartIndex + 1, returnTypeAndParams.indexOf(')'));
      details.parameterTypes = parseParameterTypes(paramsSection);
    }

    return details;
  }

  private static List<String> parseParameterTypes(String params) {
    List<String> parameterTypes = new ArrayList<>();
    if (!params.isEmpty()) {
      for (String param : params.split(",")) {
        parameterTypes.add(param.trim());
      }
    }
    return parameterTypes;
  }

  public static void main(String[] args) {
    String signature =
        "org.apache.commons.lang3.AnnotationUtils$1$isAssignableFrom__1.bootstrap$:java.util.function.Predicate(java.lang.Class)";
    MethodDetails details = parseMethodSignature(signature);

    System.out.println("Class Name: " + details.className);
    System.out.println("Method Name: " + details.methodName);
    System.out.println("Return Type: " + details.returnType);
    System.out.println("Parameter Types: " + details.parameterTypes);
  }
}
