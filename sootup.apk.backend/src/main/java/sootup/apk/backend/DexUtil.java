package sootup.apk.backend;

import static sootup.java.core.AnnotationUsage.AnnotationUsageVisibility.BUILD;

import java.util.*;
import org.apache.commons.lang3.tuple.Pair;
import org.jf.dexlib2.AnnotationVisibility;
import org.jf.dexlib2.immutable.ImmutableAnnotationElement;
import org.jf.dexlib2.immutable.reference.ImmutableFieldReference;
import org.jf.dexlib2.immutable.value.*;
import sootup.core.jimple.common.constant.*;
import sootup.core.types.*;
import sootup.java.core.AnnotationUsage;

public class DexUtil {

  /**
   * Converts a SootUp class name into a dex class name E.g., by prepending an "L", changing '.' to
   * '/', and appending an ';'
   *
   * @param s The SootUp class name
   * @return The dex class name
   */
  protected static String toDexClassName(String s) {
    int i = 0;
    while (i < s.length() && s.charAt(i) == '[') i++;

    String prefix = s.substring(0, i);
    String name = s.substring(i);

    if (isDexTypePrimitive(name)) {
      return s;
    }

    if (!name.startsWith("L")) name = "L" + name;
    if (!name.endsWith(";")) name += ";";

    return prefix + name.replace('.', '/');
  }

  /**
   * Converts a SootUp type into a dex type
   *
   * @param type The SootUp type
   * @return The dex Type
   */
  protected static String toDexType(Type type) {
    if (type == null) throw new NullPointerException("Type is null");
    if (type instanceof PrimitiveType primitiveType) return toDexPrimitiveType(primitiveType);
    if (type instanceof NullType) return "Ljava/lang/Object;";
    if (type instanceof VoidType) return "V";
    if (type instanceof ClassType classType)
      return toDexClassName(classType.getFullyQualifiedName());
    if (type instanceof ArrayType arrayType)
      return toDexArrayType(arrayType.getDimension(), arrayType);

    throw new RuntimeException("Type could not be converted to dex: " + type);
  }

  /**
   * Converts a SootUp primitive type into a dex type
   *
   * @param primitiveType The SootUp primitive type
   * @return The dex type
   */
  protected static String toDexPrimitiveType(PrimitiveType primitiveType) {
    if (primitiveType instanceof PrimitiveType.BooleanType) return "Z";
    if (primitiveType instanceof PrimitiveType.ByteType) return "B";
    if (primitiveType instanceof PrimitiveType.CharType) return "C";
    if (primitiveType instanceof PrimitiveType.ShortType) return "S";
    if (primitiveType instanceof PrimitiveType.IntType) return "I";
    if (primitiveType instanceof PrimitiveType.LongType) return "J";
    if (primitiveType instanceof PrimitiveType.DoubleType) return "D";
    if (primitiveType instanceof PrimitiveType.FloatType) return "F";
    return null;
  }

  /**
   * Evaluates whether a dex type is primitive
   *
   * @param dexType A dex type
   * @return boolean true if the type is primitive
   */
  private static boolean isDexTypePrimitive(String dexType) {
    return Set.of("Z", "B", "C", "S", "I", "J", "D", "F").contains(dexType);
  }

  /**
   * Converts a SootUp arrayType into a dex arrayType
   *
   * @param dimensions The number of dimensions of the array
   * @param arrayType The SootUp array type
   * @return The dex array type
   */
  protected static String toDexArrayType(int dimensions, ArrayType arrayType) {
    if (arrayType.getDimension() > 255) {
      throw new RuntimeException("Array " + arrayType + " has more than 255 dimensions");
    }
    return "[".repeat(Math.max(0, dimensions)) + toDexType(arrayType.getBaseType());
  }

  private static final List<Class<? extends PrimitiveType>> TYPE_ORDER =
      Arrays.asList(
          PrimitiveType.BooleanType.class,
          PrimitiveType.ByteType.class,
          PrimitiveType.CharType.class,
          PrimitiveType.ShortType.class,
          PrimitiveType.IntType.class,
          PrimitiveType.LongType.class,
          PrimitiveType.FloatType.class,
          PrimitiveType.DoubleType.class);

  private static Pair<Integer, Integer> getTypeIndexes(
      PrimitiveType sourceType, PrimitiveType compareType) {
    int indexA = TYPE_ORDER.indexOf(sourceType.getClass());
    int indexB = TYPE_ORDER.indexOf(compareType.getClass());
    if (indexA == -1) {
      throw new IllegalArgumentException("Unknown type: " + sourceType.getClass());
    } else if (indexB == -1) {
      throw new IllegalArgumentException("Unknown type: " + compareType.getClass());
    }
    return Pair.of(indexA, indexB);
  }

  /**
   * Evaluates if a SootUp primitive type is smaller than another primitive type
   *
   * @param sourceType The first SootUp type
   * @param compareType The second SootUp type
   * @return true if the first parameter has a smaller type than the second one
   */
  protected static boolean isTypeSmaller(Type sourceType, Type compareType) {
    if (!(sourceType instanceof PrimitiveType sourceTypeP)
        || !(compareType instanceof PrimitiveType compareTypeP)) {
      return false;
    }
    Pair<Integer, Integer> indexes = getTypeIndexes(sourceTypeP, compareTypeP);
    return indexes.getLeft() < indexes.getRight();
  }

  /**
   * Evaluates if a SootUp primitive type is smaller or equal than another primitive type
   *
   * @param sourceType The first SootUp type
   * @param compareType The second SootUp type
   * @return true if the first parameter has a smaller or equal type than the second one
   */
  protected static boolean isTypeSmallerOrEqual(Type sourceType, Type compareType) {
    if (!(sourceType instanceof PrimitiveType sourceTypeP)
        || !(compareType instanceof PrimitiveType compareTypeP)) {
      return false;
    }
    Pair<Integer, Integer> indexes = getTypeIndexes(sourceTypeP, compareTypeP);
    return indexes.getLeft() <= indexes.getRight();
  }

  /**
   * Evaluates if a SootUp primitive type is bigger or equal than another primitive type
   *
   * @param sourceType The first SootUp type
   * @param compareType The second SootUp type
   * @return true if the first parameter has a bigger or equal type than the second one
   */
  protected static boolean isTypeBiggerOrEqual(Type sourceType, Type compareType) {
    if (!(sourceType instanceof PrimitiveType sourceTypeP)
        || !(compareType instanceof PrimitiveType compareTypeP)) {
      return false;
    }
    Pair<Integer, Integer> indexes = getTypeIndexes(sourceTypeP, compareTypeP);
    return indexes.getLeft() >= indexes.getRight();
  }

  /**
   * Evaluates if a SootUp primitive type is bigger or equal than another primitive type
   *
   * @param sourceType The first SootUp type
   * @param compareType The second SootUp type
   * @return true if the first parameter has a bigger or equal type than the second one
   */
  protected static boolean isTypeBigger(Type sourceType, Type compareType) {
    if (!(sourceType instanceof PrimitiveType sourceTypeP)
        || !(compareType instanceof PrimitiveType compareTypeP)) {
      return false;
    }
    Pair<Integer, Integer> indexes = getTypeIndexes(sourceTypeP, compareTypeP);
    return indexes.getLeft() > indexes.getRight();
  }

  /**
   * Returns the biggest type of two SootUp primitive types
   *
   * @param op1 The first SootUp type
   * @param op2 The second SootUp type
   * @return The type of the two parameters that is the biggest
   */
  protected static PrimitiveType getArithmeticType(PrimitiveType op1, PrimitiveType op2) {
    if (isTypeSmallerOrEqual(op1, PrimitiveType.getInt())
        && isTypeSmallerOrEqual(op2, PrimitiveType.getInt())) {
      return PrimitiveType.getInt();
    }
    return isTypeSmaller(op1, op2) ? op2 : op1;
  }

  /**
   * Evaluates whether a dex type is of an object type
   *
   * @param dexType The dex type
   * @return true if the type is of an object type
   */
  protected static boolean isObject(String dexType) {
    return dexType != null
        && !dexType.isEmpty()
        && (dexType.charAt(0) == 'L' || dexType.charAt(0) == '[');
  }

  /**
   * Evaluates whether a SootUp type is of an object type
   *
   * @param sootType The SootUp type
   * @return true if the type is of an object type
   */
  protected static boolean isObject(Type sootType) {
    return isObject(toDexType(sootType));
  }

  /**
   * Evaluates whether a dex type is wide (of type long or double)
   *
   * @param dexType The dex type
   * @return True if the type is long or double
   */
  protected static boolean isWide(String dexType) {
    return dexType != null && (dexType.equals("J") || dexType.equals("D"));
  }

  /**
   * Evaluates whether a SootUp type is wide (of type long or double)
   *
   * @param sootType The SootUp type
   * @return True if the type is long or double
   */
  protected static boolean isWide(Type sootType) {
    return sootType instanceof PrimitiveType.LongType
        || sootType instanceof PrimitiveType.DoubleType;
  }

  /**
   * Returns the size of the register for a SootUp type
   *
   * @param sootType The SootUp type
   * @return 2 if the type is long or double, else 1
   */
  public static int getRegisterSizeCount(Type sootType) {
    return isWide(sootType) ? 2 : 1;
  }

  /**
   * Sum of the register sizes for a collection of SootUp types
   *
   * @param sootTypes A collection of SootUp types
   * @return A summed register size needed to store all types of the collection
   */
  public static int getRegisterSizeCount(Collection<Type> sootTypes) {
    return sootTypes.stream().mapToInt(DexUtil::getRegisterSizeCount).sum();
  }

  /**
   * Sum of the register sizes for a collection of Register if register type is long or double ->
   * count 2 registers; else 1 register
   *
   * @param registers A list of Register objects
   * @return The sum of the register sizes
   */
  public static int getRegisterSizeCount(List<Register> registers) {
    return registers.stream().mapToInt(Register::getSize).sum();
  }

  protected static boolean inSigned4Bit(long number) {
    return number >= -8 && number <= 7;
  }

  protected static boolean inSigned8Bit(long number) {
    return number >= -128 && number <= 127;
  }

  protected static boolean inSigned16Bit(long number) {
    return number >= -32768 && number <= 32767;
  }

  protected static boolean inSigned32Bit(long number) {
    return number >= -2147483648 && number <= 2147483647;
  }

  /**
   * Converts a jimple annotation visibility into a dex annotation visibility
   *
   * @param annotationUsageVisibility Jimple annotation visibility
   * @return enum element dex annotation visibility
   */
  protected static int convertAnnotationVisibility(
      AnnotationUsage.AnnotationUsageVisibility annotationUsageVisibility) {
    return switch (annotationUsageVisibility) {
      case BUILD -> AnnotationVisibility.BUILD;
      case RUNTIME -> AnnotationVisibility.RUNTIME;

      case SYSTEM -> AnnotationVisibility.SYSTEM;
      case NONE -> -1;
    };
  }

  /**
   * Converts SootUp annotation values into dex ImmutableEncodedValue
   *
   * @param elem A SootUp annotation value (created in
   *     sootup.apk.frontend.Util->convertAnnotationValue()
   * @return A dex ImmutableEncodedValue
   */
  public static ImmutableEncodedValue buildEncodedValueForAnnotation(Object elem) {
    if (elem instanceof BooleanConstant booleanConstant) {
      return booleanConstant.getValue()
          ? ImmutableBooleanEncodedValue.TRUE_VALUE
          : ImmutableBooleanEncodedValue.FALSE_VALUE;
    } else if (elem instanceof IntConstant intConstant) {
      return new ImmutableIntEncodedValue(intConstant.getValue());
    } else if (elem instanceof LongConstant longConstant) {
      return new ImmutableLongEncodedValue(longConstant.getValue());
    } else if (elem instanceof FloatConstant floatConstant) {
      return new ImmutableFloatEncodedValue(floatConstant.getValue());
    } else if (elem instanceof DoubleConstant doubleConstant) {
      return new ImmutableDoubleEncodedValue(doubleConstant.getValue());
    } else if (elem instanceof StringConstant stringConstant) {
      return new ImmutableStringEncodedValue(stringConstant.getValue());
    } else if (elem instanceof ClassConstant classConstant) {
      return new ImmutableTypeEncodedValue(classConstant.getValue());
    } else if (elem instanceof EnumConstant enumConstant) {
      String classT = DexUtil.toDexType(enumConstant.getType());
      return new ImmutableEnumEncodedValue(
          new ImmutableFieldReference(classT, enumConstant.getValue(), classT));
    } else if (elem instanceof AnnotationUsage annotationUsage) {
      List<ImmutableAnnotationElement> elements = new ArrayList<>();
      for (var a : annotationUsage.getValues().entrySet()) {
        ImmutableAnnotationElement e =
            new ImmutableAnnotationElement(
                a.getKey(), DexUtil.buildEncodedValueForAnnotation(a.getValue()));
        elements.add(e);
      }
      return new ImmutableAnnotationEncodedValue(
          DexUtil.toDexClassName(annotationUsage.getAnnotation().getFullyQualifiedName()),
          elements);

    } else if (elem instanceof List list) {
      List<ImmutableEncodedValue> values = new ArrayList<>();
      for (var e : list) {
        values.add(DexUtil.buildEncodedValueForAnnotation(e));
      }
      return new ImmutableArrayEncodedValue(values);
    } else if (elem instanceof NullConstant) {
      return ImmutableNullEncodedValue.INSTANCE;
    }
    return ImmutableNullEncodedValue.INSTANCE;
  }
}
