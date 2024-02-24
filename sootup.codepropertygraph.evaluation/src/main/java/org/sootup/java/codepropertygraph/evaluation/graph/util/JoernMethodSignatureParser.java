package org.sootup.java.codepropertygraph.evaluation.graph.util;

import java.util.ArrayList;
import java.util.List;
import org.sootup.java.codepropertygraph.evaluation.graph.model.JoernMethodDetails;

public class JoernMethodSignatureParser {
  public static JoernMethodDetails parseMethodSignature(String signature) {

    // Split the signature by ':' to separate the class.method from return type and parameters
    String[] parts = signature.split(":");

    if (parts.length != 2) {
      throw new RuntimeException("Invalid Joern method signature: " + signature);
    }

    // Extract class name and method name
    int lastDotIndex = parts[0].lastIndexOf('.');
    String className = parts[0].substring(0, lastDotIndex);
    String methodName = parts[0].substring(lastDotIndex + 1);

    // Extract return type
    String returnTypeAndParams = parts[1];
    int paramsStartIndex = returnTypeAndParams.indexOf('(');
    String returnType = returnTypeAndParams.substring(0, paramsStartIndex);

    // Extract parameter types
    String paramsSection =
        returnTypeAndParams.substring(paramsStartIndex + 1, returnTypeAndParams.indexOf(')'));
    List<String> parameterTypes = parseParameterTypes(paramsSection);

    return new JoernMethodDetails(className, methodName, parameterTypes, returnType);
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
}
