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
import org.jf.dexlib2.ValueType;
import org.jf.dexlib2.iface.Annotation;
import org.jf.dexlib2.iface.AnnotationElement;
import org.jf.dexlib2.iface.reference.FieldReference;
import org.jf.dexlib2.iface.value.*;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import sootup.apk.frontend.main.AndroidVersionInfo;
import sootup.core.IdentifierFactory;
import sootup.core.types.*;
import sootup.core.views.View;
import sootup.java.core.AnnotationUsage;
import sootup.java.core.ConstantUtil;
import sootup.java.core.language.JavaJimple;

public class DexUtil {

  private static AndroidVersionInfo androidVersionInfo;

  public static Type toSootType(
      String typeDescriptor, int pos, @NonNull IdentifierFactory identifierFactory) {
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
        type = getClassTypeFromClassName(typeDescriptor, identifierFactory);
        break;
      case 'V': // void
        type = VoidType.getInstance();
        break;
      case '[': // array
        Type sootType = toSootType(typeDescriptor, pos + 1, identifierFactory);
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
   * Converts the annotations of a dex class, field, method or parameter. System annotations (<code>
   * dalvik.annotation.*</code>, visibility SYSTEM) encode classfile attributes like Throws,
   * Signature or InnerClass and are skipped, like the bytecode frontend does not report attributes
   * as annotations. BUILD annotations are runtime invisible, RUNTIME ones runtime visible.
   */
  @NonNull
  public static List<AnnotationUsage> createAnnotationUsage(
      @NonNull Set<? extends Annotation> annotations,
      @NonNull IdentifierFactory identifierFactory) {
    if (annotations.isEmpty()) {
      return Collections.emptyList();
    }
    List<AnnotationUsage> annotationUsages = new ArrayList<>(annotations.size());
    for (Annotation annotation : annotations) {
      if (annotation.getVisibility() == AnnotationVisibility.SYSTEM) {
        continue;
      }
      boolean runtimeVisible = annotation.getVisibility() == AnnotationVisibility.RUNTIME;
      annotationUsages.add(
          createAnnotationUsage(
              annotation.getType(), annotation.getElements(), identifierFactory, runtimeVisible));
    }
    return annotationUsages;
  }

  @NonNull
  private static AnnotationUsage createAnnotationUsage(
      @NonNull String type,
      @NonNull Set<? extends AnnotationElement> elements,
      @NonNull IdentifierFactory identifierFactory,
      boolean runtimeVisible) {
    Map<String, Object> paramMap = new LinkedHashMap<>();
    for (AnnotationElement element : elements) {
      Object value = convertAnnotationValue(element.getValue(), identifierFactory, runtimeVisible);
      if (value != null) {
        paramMap.put(element.getName(), value);
      }
    }
    return new AnnotationUsage(
        identifierFactory.getClassType(toQualifiedName(type)), paramMap, runtimeVisible);
  }

  /**
   * Converts an annotation element value into the representation of the bytecode frontend:
   * constants, nested {@link AnnotationUsage}s, or lists for arrays.
   *
   * @return the converted value, or null for values that Java annotations cannot hold (field,
   *     method, method type and method handle references)
   */
  private static Object convertAnnotationValue(
      @NonNull EncodedValue value,
      @NonNull IdentifierFactory identifierFactory,
      boolean runtimeVisible) {
    switch (value.getValueType()) {
      case ValueType.BYTE:
        return ConstantUtil.fromObject(((ByteEncodedValue) value).getValue(), identifierFactory);
      case ValueType.SHORT:
        return ConstantUtil.fromObject(((ShortEncodedValue) value).getValue(), identifierFactory);
      case ValueType.CHAR:
        return ConstantUtil.fromObject(((CharEncodedValue) value).getValue(), identifierFactory);
      case ValueType.INT:
        return ConstantUtil.fromObject(((IntEncodedValue) value).getValue(), identifierFactory);
      case ValueType.LONG:
        return ConstantUtil.fromObject(((LongEncodedValue) value).getValue(), identifierFactory);
      case ValueType.FLOAT:
        return ConstantUtil.fromObject(((FloatEncodedValue) value).getValue(), identifierFactory);
      case ValueType.DOUBLE:
        return ConstantUtil.fromObject(((DoubleEncodedValue) value).getValue(), identifierFactory);
      case ValueType.BOOLEAN:
        return ConstantUtil.fromObject(((BooleanEncodedValue) value).getValue(), identifierFactory);
      case ValueType.STRING:
        return ConstantUtil.fromObject(((StringEncodedValue) value).getValue(), identifierFactory);
      case ValueType.NULL:
        return ConstantUtil.fromObject(null, identifierFactory);
      case ValueType.TYPE:
        return JavaJimple.newClassConstant(
            ((TypeEncodedValue) value).getValue(), identifierFactory);
      case ValueType.ENUM:
        FieldReference enumField = ((EnumEncodedValue) value).getValue();
        return ConstantUtil.fromObject(
            new String[] {toQualifiedName(enumField.getDefiningClass()), enumField.getName()},
            identifierFactory);
      case ValueType.ANNOTATION:
        AnnotationEncodedValue annotation = (AnnotationEncodedValue) value;
        return createAnnotationUsage(
            annotation.getType(), annotation.getElements(), identifierFactory, runtimeVisible);
      case ValueType.ARRAY:
        List<Object> values = new ArrayList<>();
        for (EncodedValue element : ((ArrayEncodedValue) value).getValue()) {
          Object converted = convertAnnotationValue(element, identifierFactory, runtimeVisible);
          if (converted != null) {
            values.add(converted);
          }
        }
        return values;
      default:
        return null;
    }
  }

  /**
   * Extracts the declared exceptions of a method from its <code>dalvik.annotation.Throws</code>
   * system annotation.
   */
  @NonNull
  public static List<ClassType> extractThrownExceptions(
      @NonNull Set<? extends Annotation> annotations,
      @NonNull IdentifierFactory identifierFactory) {
    for (Annotation annotation : annotations) {
      if (!THROWS_ANNOTATION.equals(annotation.getType())) {
        continue;
      }
      for (AnnotationElement element : annotation.getElements()) {
        if (!"value".equals(element.getName())
            || !(element.getValue() instanceof ArrayEncodedValue)) {
          continue;
        }
        List<ClassType> exceptions = new ArrayList<>();
        for (EncodedValue exception : ((ArrayEncodedValue) element.getValue()).getValue()) {
          if (exception instanceof TypeEncodedValue) {
            exceptions.add(
                getClassTypeFromClassName(
                    ((TypeEncodedValue) exception).getValue(), identifierFactory));
          }
        }
        return exceptions;
      }
    }
    return Collections.emptyList();
  }

  private static final String THROWS_ANNOTATION = "Ldalvik/annotation/Throws;";

  private static final String ANNOTATION_DEFAULT = "Ldalvik/annotation/AnnotationDefault;";

  /**
   * Returns the default values of the methods of an annotation by method name. Dex keeps them in
   * the <code>dalvik.annotation.AnnotationDefault</code> system annotation of the annotation class,
   * as an annotation of that class whose elements are the defaults.
   */
  @NonNull
  public static Map<String, EncodedValue> getAnnotationDefaults(
      @NonNull Set<? extends Annotation> classAnnotations) {
    for (Annotation annotation : classAnnotations) {
      if (!annotation.getType().equals(ANNOTATION_DEFAULT)) {
        continue;
      }
      for (AnnotationElement element : annotation.getElements()) {
        if (element.getName().equals("value")
            && element.getValue() instanceof AnnotationEncodedValue) {
          Map<String, EncodedValue> defaults = new HashMap<>();
          for (AnnotationElement method :
              ((AnnotationEncodedValue) element.getValue()).getElements()) {
            defaults.put(method.getName(), method.getValue());
          }
          return defaults;
        }
      }
    }
    return Collections.emptyMap();
  }

  /**
   * Converts the default value of an annotation method like the bytecode frontend does: nested
   * annotations are runtime visible.
   */
  @Nullable
  public static Object convertAnnotationDefault(
      @NonNull EncodedValue value, @NonNull IdentifierFactory identifierFactory) {
    return convertAnnotationValue(value, identifierFactory, true);
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

  public static ClassType getClassTypeFromClassName(
      String name, @NonNull IdentifierFactory identifierFactory) {
    if (name.startsWith("[")) {
      name = "java.lang.Object";
    } else if (isByteCodeClassName(name)) {
      name = dottedClassName(name);
    }
    ClassType javaClassType;
    try {
      javaClassType = identifierFactory.getClassType(name);
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
