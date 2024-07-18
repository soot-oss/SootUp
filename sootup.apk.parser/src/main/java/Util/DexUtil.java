package Util;

import java.util.*;
import javax.annotation.Nonnull;
import org.jf.dexlib2.iface.Annotation;
import org.jf.dexlib2.iface.AnnotationElement;
import org.jf.dexlib2.iface.value.EncodedValue;
import sootup.core.signatures.PackageName;
import sootup.core.types.*;
import sootup.core.views.View;
import sootup.java.core.AnnotationUsage;
import sootup.java.core.types.JavaClassType;

public class DexUtil {

  public static Type toSootType(String typeDescriptor, int pos) {
    Type type = null;
    char typeDesignator = typeDescriptor.charAt(pos);
    switch (typeDesignator) {
      case 'Z': // boolean
        type = PrimitiveType.BooleanType.getInstance();
        break;
      case 'B': // byte
        type = PrimitiveType.ByteType.getInstance();
        break;
      case 'S': // short
        type = PrimitiveType.ShortType.getInstance();
        break;
      case 'C': // char
        type = PrimitiveType.CharType.getInstance();
        break;
      case 'I': // int
        type = PrimitiveType.IntType.getInstance();
        break;
      case 'J': // long
        type = PrimitiveType.LongType.getInstance();
        break;
      case 'F': // float
        type = PrimitiveType.FloatType.getInstance();
        break;
      case 'D': // double
        type = PrimitiveType.DoubleType.getInstance();
        break;
      case 'L': // object
        if (isByteCodeClassName(typeDescriptor)) {
          typeDescriptor = dottedClassName(typeDescriptor);
        }
        type = getClassTypeFromClassName(typeDescriptor);
        break;
      case 'V': // void
        type = VoidType.getInstance();
        break;
      case '[': // array
        Type sootType = toSootType(typeDescriptor, pos + 1);
        if (sootType != null) {
          type = Type.createArrayType(sootType, 1);
        }
        break;
      default:
        type = UnknownType.getInstance();
    }
    return type;
  }

  public static String toQualifiedName(@Nonnull String str) {
    final int endpos = str.length() - 1;
    if (endpos > 2 && str.charAt(endpos) == ';' && str.charAt(0) == 'L') {
      str = str.substring(1, endpos);
    }
    return str.replace('/', '.');
  }

  public static Iterable<AnnotationUsage> createAnnotationUsage(
      Set<? extends Annotation> annotations) {
    if (annotations.isEmpty()) {
      return Collections.emptyList();
    }
    Map<String, Object> paramMap = new HashMap<>();
    for (Annotation annotation : annotations) {
      for (AnnotationElement element : annotation.getElements()) {
        final String annotationName = element.getName();
        EncodedValue value = element.getValue();
      }
    }
    return null;
  }

  public static ClassType stringToJimpleType(View view, String className) {
    return view.getIdentifierFactory().getClassType(DexUtil.toQualifiedName(className));
  }

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
