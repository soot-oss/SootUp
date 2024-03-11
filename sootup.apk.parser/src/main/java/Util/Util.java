package Util;

import instruction.DexLibAbstractInstruction;
import instruction.ReturnVoidInstruction;
import java.util.EnumSet;
import org.jf.dexlib2.iface.Method;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.MethodModifier;
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

  public static Stmt makeStmt(DexLibAbstractInstruction ins) {
    Stmt stmt = null;
    if (ins instanceof ReturnVoidInstruction) {
      stmt = Jimple.newReturnVoidStmt(StmtPositionInfo.getNoStmtPositionInfo());
    }
    return stmt;
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

  public EnumSet<MethodModifier> convertModifiers(Method method) {
    method.getAccessFlags();
    EnumSet<MethodModifier> modifiers = EnumSet.noneOf(MethodModifier.class);

    return modifiers;
  }
}
