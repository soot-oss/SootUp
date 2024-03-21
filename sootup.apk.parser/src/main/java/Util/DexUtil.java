package Util;

import java.util.*;
import javax.annotation.Nonnull;
import org.jf.dexlib2.iface.Annotation;
import org.jf.dexlib2.iface.AnnotationElement;
import org.jf.dexlib2.iface.value.EncodedValue;
import sootup.core.model.ClassModifier;
import sootup.core.model.FieldModifier;
import sootup.core.types.PrimitiveType;
import sootup.core.types.Type;
import sootup.core.types.UnknownType;
import sootup.core.types.VoidType;
import sootup.java.core.AnnotationUsage;
import sootup.java.core.JavaIdentifierFactory;
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
        if (Util.isByteCodeClassName(typeDescriptor)) {
          typeDescriptor = Util.dottedClassName(typeDescriptor);
        }
        type = Util.getClassTypeFromClassName(typeDescriptor);
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

  public static EnumSet<ClassModifier> getClassModifiers(int access) {
    EnumSet<ClassModifier> modifierEnumSet = EnumSet.noneOf(ClassModifier.class);

    // add all modifiers for which (access & ABSTRACT) =! 0
    for (ClassModifier modifier : ClassModifier.values()) {
      if ((access & modifier.getBytecode()) != 0) {
        modifierEnumSet.add(modifier);
      }
    }
    return modifierEnumSet;
  }

  public static EnumSet<FieldModifier> getFieldModifiers(int access) {
    EnumSet<FieldModifier> modifierEnumSet = EnumSet.noneOf(FieldModifier.class);

    // add all modifiers for which (access & ABSTRACT) =! 0
    for (FieldModifier modifier : FieldModifier.values()) {
      if ((access & modifier.getBytecode()) != 0) {
        modifierEnumSet.add(modifier);
      }
    }
    return modifierEnumSet;
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

  public static JavaClassType stringToJimpleType(String className) {
    return JavaIdentifierFactory.getInstance().getClassType(DexUtil.toQualifiedName(className));
  }
}
