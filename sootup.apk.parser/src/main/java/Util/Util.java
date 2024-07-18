package Util;

import sootup.core.signatures.PackageName;
import sootup.core.types.ClassType;
import sootup.java.core.types.JavaClassType;

public class Util {
  public static String dottedClassName(String typeDescriptor) {
    String t = typeDescriptor;
    int idx = 0;
    while (idx < t.length() && t.charAt(idx) == '[') {
      idx++;
    }
    String className = typeDescriptor.substring(idx);

    className = className.substring(className.indexOf('L') + 1, className.indexOf(';'));

    className = className.replace('/', '.');

    return className;
  }

  public static boolean isByteCodeClassName(String className) {
    return ((className.startsWith("L") || className.startsWith("["))
        && className.endsWith(";")
        && ((className.indexOf('/') != -1 || className.indexOf('.') == -1)));
  }

  public static ClassType getClassTypeFromClassName(String name) {
    if (name.startsWith("[")) {
      name = "java.lang.Object";
    } else if (isByteCodeClassName(name)) {
      name = dottedClassName(name);
    }
    JavaClassType javaClassType;
    int lastIndex = name.lastIndexOf(".");
    lastIndex = (lastIndex == -1) ? 0 : lastIndex;
    try {
      javaClassType =
          new JavaClassType(
              name.substring(name.lastIndexOf(".") + 1),
              new PackageName(name.substring(0, lastIndex)));
    } catch (Exception exception) {
      System.out.println("Exception when substring with className " + name);
      throw new RuntimeException();
    }

    return javaClassType;
  }
}
