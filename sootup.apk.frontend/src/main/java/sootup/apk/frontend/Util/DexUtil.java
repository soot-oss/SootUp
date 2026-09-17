package sootup.apk.frontend.Util;

/*-
 * #%L
 * SootUp
 * %%
 * Copyright (C) 2022 - 2024 Kadiray Karakaya, Markus Schmidt, Jonas Klauke, Stefan Schott, Palaniappan Muthuraman, Marcus Hüwe and others
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import java.util.*;
import org.jf.dexlib2.AnnotationVisibility;
import org.jf.dexlib2.iface.Annotation;
import org.jf.dexlib2.iface.AnnotationElement;
import org.jf.dexlib2.iface.reference.FieldReference;
import org.jf.dexlib2.iface.value.*;
import org.jspecify.annotations.NonNull;
import sootup.core.jimple.common.constant.*;
import sootup.core.types.*;
import sootup.core.views.View;
import sootup.java.core.AnnotationUsage;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.language.JavaJimple;
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

  public static String toQualifiedName(@NonNull String str) {
    final int endpos = str.length() - 1;
    if (endpos > 2 && str.charAt(endpos) == ';' && str.charAt(0) == 'L') {
      str = str.substring(1, endpos);
    }
    return str.replace('/', '.');
  }

  /**
   * Converts dex annotations like {@code AsmUtil.createAnnotationUsage}. System annotations
   * (dalvik.annotation.*) describe class metadata such as inner classes or throws, so they are left
   * out.
   */
  public static List<AnnotationUsage> createAnnotationUsage(Set<? extends Annotation> annotations) {
    List<AnnotationUsage> usages = new ArrayList<>();
    for (Annotation annotation : annotations) {
      if (annotation.getVisibility() != AnnotationVisibility.SYSTEM) {
        usages.add(createAnnotationUsage(annotation.getType(), annotation.getElements()));
      }
    }
    return usages;
  }

  /** The exceptions of a method, which dex stores in its dalvik.annotation.Throws annotation. */
  public static List<ClassType> getThrownExceptions(Set<? extends Annotation> annotations) {
    List<ClassType> exceptions = new ArrayList<>();
    for (Annotation annotation : annotations) {
      if (!annotation.getType().equals("Ldalvik/annotation/Throws;")) {
        continue;
      }
      for (AnnotationElement element : annotation.getElements()) {
        if (element.getName().equals("value") && element.getValue() instanceof ArrayEncodedValue) {
          for (EncodedValue value : ((ArrayEncodedValue) element.getValue()).getValue()) {
            if (value instanceof TypeEncodedValue) {
              exceptions.add(
                  JavaIdentifierFactory.getInstance()
                      .getClassType(toQualifiedName(((TypeEncodedValue) value).getValue())));
            }
          }
        }
      }
    }
    return exceptions;
  }

  private static AnnotationUsage createAnnotationUsage(
      String type, Set<? extends AnnotationElement> elements) {
    Map<String, Object> values = new HashMap<>();
    for (AnnotationElement element : elements) {
      values.put(element.getName(), convertAnnotationValue(element.getValue()));
    }
    return new AnnotationUsage(
        JavaIdentifierFactory.getInstance().getClassType(toQualifiedName(type)), values);
  }

  private static Object convertAnnotationValue(EncodedValue value) {
    if (value instanceof BooleanEncodedValue) {
      return BooleanConstant.getInstance(((BooleanEncodedValue) value).getValue());
    } else if (value instanceof ByteEncodedValue) {
      return IntConstant.getInstance(((ByteEncodedValue) value).getValue());
    } else if (value instanceof ShortEncodedValue) {
      return IntConstant.getInstance(((ShortEncodedValue) value).getValue());
    } else if (value instanceof CharEncodedValue) {
      return IntConstant.getInstance(((CharEncodedValue) value).getValue());
    } else if (value instanceof IntEncodedValue) {
      return IntConstant.getInstance(((IntEncodedValue) value).getValue());
    } else if (value instanceof LongEncodedValue) {
      return LongConstant.getInstance(((LongEncodedValue) value).getValue());
    } else if (value instanceof FloatEncodedValue) {
      return FloatConstant.getInstance(((FloatEncodedValue) value).getValue());
    } else if (value instanceof DoubleEncodedValue) {
      return DoubleConstant.getInstance(((DoubleEncodedValue) value).getValue());
    } else if (value instanceof StringEncodedValue) {
      return JavaJimple.newStringConstant(((StringEncodedValue) value).getValue());
    } else if (value instanceof TypeEncodedValue) {
      return JavaJimple.newClassConstant(((TypeEncodedValue) value).getValue());
    } else if (value instanceof EnumEncodedValue) {
      FieldReference constant = ((EnumEncodedValue) value).getValue();
      return JavaJimple.newEnumConstant(
          constant.getName(), toQualifiedName(constant.getDefiningClass()));
    } else if (value instanceof AnnotationEncodedValue) {
      AnnotationEncodedValue nested = (AnnotationEncodedValue) value;
      return createAnnotationUsage(nested.getType(), nested.getElements());
    } else if (value instanceof ArrayEncodedValue) {
      List<Object> elements = new ArrayList<>();
      for (EncodedValue element : ((ArrayEncodedValue) value).getValue()) {
        elements.add(convertAnnotationValue(element));
      }
      return elements;
    } else if (value instanceof NullEncodedValue) {
      return NullConstant.getInstance();
    }
    // method, field, method type and method handle values only occur in system annotations
    return JavaJimple.newStringConstant(value.toString());
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
    try {
      javaClassType = JavaIdentifierFactory.getInstance().getClassType(name);
    } catch (Exception exception) {
      System.out.println("Exception when substring with className " + name);
      throw new RuntimeException();
    }

    return javaClassType;
  }
}
