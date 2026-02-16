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
import org.jf.dexlib2.dexbacked.raw.EncodedValue;
import org.jf.dexlib2.dexbacked.value.DexBackedArrayEncodedValue;
import org.jf.dexlib2.iface.Annotation;
import org.jf.dexlib2.iface.AnnotationElement;
import org.jspecify.annotations.NonNull;
import sootup.apk.frontend.main.AndroidVersionInfo;
import sootup.core.jimple.common.constant.ClassConstant;
import sootup.core.signatures.PackageName;
import sootup.core.types.*;
import sootup.core.views.View;
import sootup.java.core.AnnotationUsage;
import sootup.java.core.ConstantUtil;
import sootup.java.core.language.JavaJimple;
import sootup.java.core.types.JavaClassType;

public class DexUtil {

  private static AndroidVersionInfo androidVersionInfo;

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

  public static Iterable<ClassType> extractThrownExceptions(
      Set<? extends Annotation> annotations, View view) {
    for (Annotation annotation : annotations) {
      ClassType at =
          view.getIdentifierFactory().getClassType(DexUtil.toQualifiedName(annotation.getType()));

      if (at.getPackageName().getName().equals("dalvik.annotation")
          && at.getClassName().equals("Throws")) {
        for (AnnotationElement element : annotation.getElements()) {
          var exceptionsList = ((DexBackedArrayEncodedValue) element.getValue()).getValue();
          return exceptionsList.stream()
              .map(encodedValue -> (ClassType) toSootType(encodedValue.toString(), 0))
              .toList();
        }
      }
    }

    return Collections.emptyList();
  }

  public static Iterable<AnnotationUsage> createAnnotationUsage(
      Set<? extends Annotation> annotations, View view) {
    if (annotations.isEmpty()) {
      return Collections.emptyList();
    }

    ArrayList<AnnotationUsage> annotationUsage = new ArrayList<>();
    Map<String, Object> paramMap = new HashMap<>();

    for (Annotation annotation : annotations) {
      ClassType at =
          view.getIdentifierFactory().getClassType(DexUtil.toQualifiedName(annotation.getType()));

      if (!at.getPackageName().getName().equals("dalvik.annotation")
          || !at.getClassName().equals("Throws")) {
        for (AnnotationElement element : annotation.getElements()) {
          String name = element.getName();
          paramMap.put(name, convertAnnotationValue(element.getValue().getValueType()));
        }
        annotationUsage.add(new AnnotationUsage(at, paramMap));
      }
    }

    return annotationUsage;
  }

  private static Object convertAnnotationValue(Object annotationValue) {
    if (annotationValue instanceof EncodedValue) {
      ClassConstant classConstant = JavaJimple.newClassConstant(annotationValue.toString());
      return ConstantUtil.fromObject(classConstant);
    }
    return ConstantUtil.fromObject(annotationValue);
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

  public static void setAndroidVersionInfo(AndroidVersionInfo androidVersionInfo) {
    DexUtil.androidVersionInfo = androidVersionInfo;
  }

  public static AndroidVersionInfo getAndroidVersionInfo() {
    return androidVersionInfo;
  }
}
