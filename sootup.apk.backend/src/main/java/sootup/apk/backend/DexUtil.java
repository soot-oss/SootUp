package sootup.apk.backend;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.tuple.Pair;
import sootup.core.types.*;

public class DexUtil {

  protected static String toDexClassName(String sootClassName) {
    if (!sootClassName.startsWith("L") && !sootClassName.endsWith(";")) {
      sootClassName = 'L' + sootClassName + ';';
    }
    return sootClassName.replace('.', '/');
  }

  protected static String toDexType(Type type) {
    if (type == null) {
      throw new NullPointerException("Type is null");
    } else if (type instanceof PrimitiveType primitiveType) {
      return toDexPrimitiveType(primitiveType);
    } else if (type instanceof NullType) {
      return "I";
    } else if (type instanceof VoidType) {
      return "V";
    } else if (type instanceof ClassType classType) {
      return toDexClassName(classType.getFullyQualifiedName());
    } else if (type instanceof ArrayType arrayType) {
      return toDexArrayType(arrayType.getDimension(), arrayType);
    } else {
      throw new RuntimeException("Type could not be converted to dex: " + type);
    }
  }

  protected static String toDexPrimitiveType(PrimitiveType primitiveType) {
    if (primitiveType instanceof PrimitiveType.BooleanType) {
      return "Z";
    }
    if (primitiveType instanceof PrimitiveType.ByteType) {
      return "B";
    }
    if (primitiveType instanceof PrimitiveType.CharType) {
      return "C";
    }
    if (primitiveType instanceof PrimitiveType.ShortType) {
      return "S";
    }
    if (primitiveType instanceof PrimitiveType.IntType) {
      return "I";
    }
    if (primitiveType instanceof PrimitiveType.LongType) {
      return "J";
    }
    if (primitiveType instanceof PrimitiveType.DoubleType) {
      return "D";
    }
    if (primitiveType instanceof PrimitiveType.FloatType) {
      return "F";
    }
    return null;
  }

  private boolean isDexTypePrimitive(String dexType) {
    return Set.of("Z", "B", "C", "S", "I", "J", "D", "F").contains(dexType);
  }

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

  protected static boolean isTypeSmaller(Type sourceType, Type compareType) {
    if (!(sourceType instanceof PrimitiveType sourceTypeP)
        || !(compareType instanceof PrimitiveType compareTypeP)) {
      return false;
    }
    Pair<Integer, Integer> indexes = getTypeIndexes(sourceTypeP, compareTypeP);
    return indexes.getLeft() < indexes.getRight();
  }

  protected static boolean isTypeSmallerOrEqual(Type sourceType, Type compareType) {
    if (!(sourceType instanceof PrimitiveType sourceTypeP)
        || !(compareType instanceof PrimitiveType compareTypeP)) {
      return false;
    }
    Pair<Integer, Integer> indexes = getTypeIndexes(sourceTypeP, compareTypeP);
    return indexes.getLeft() <= indexes.getRight();
  }

  protected static boolean isTypeBiggerOrEqual(Type sourceType, Type compareType) {
    if (!(sourceType instanceof PrimitiveType sourceTypeP)
        || !(compareType instanceof PrimitiveType compareTypeP)) {
      return false;
    }
    Pair<Integer, Integer> indexes = getTypeIndexes(sourceTypeP, compareTypeP);
    return indexes.getLeft() >= indexes.getRight();
  }

  protected static boolean isTypeBigger(Type sourceType, Type compareType) {
    if (!(sourceType instanceof PrimitiveType sourceTypeP)
        || !(compareType instanceof PrimitiveType compareTypeP)) {
      return false;
    }
    Pair<Integer, Integer> indexes = getTypeIndexes(sourceTypeP, compareTypeP);
    return indexes.getLeft() > indexes.getRight();
  }

  protected static boolean isTypeEqualOrBigger(Type sourceType, Type compareType) {
    if (!(sourceType instanceof PrimitiveType sourceTypeP)
        || !(compareType instanceof PrimitiveType compareTypeP)) {
      return false;
    }
    Pair<Integer, Integer> indexes = getTypeIndexes(sourceTypeP, compareTypeP);
    return indexes.getLeft() >= indexes.getRight();
  }

  protected static PrimitiveType getArithmeticType(PrimitiveType op1, PrimitiveType op2) {
    if (isTypeSmallerOrEqual(op1, PrimitiveType.getInt())
        && isTypeSmallerOrEqual(op2, PrimitiveType.getInt())) {
      return PrimitiveType.getInt();
    }
    if (isTypeSmaller(op1, op2)) {
      return op2;
    } else {
      return op1;
    }
  }

  protected static boolean isObject(String dexType) {
    if (dexType == null) {
      return false;
    }
    if (dexType.isEmpty()) {
      return false;
    }
    char first = dexType.charAt(0);
    return first == 'L' || first == '[';
  }

  protected static boolean isObject(Type sootType) {
    return isObject(toDexType(sootType));
  }

  protected static boolean isWide(String dexType) {
    return dexType != null && (dexType.equals("J") || dexType.equals("D"));
  }

  protected static boolean isWide(Type sootType) {
    return sootType instanceof PrimitiveType.LongType
        || sootType instanceof PrimitiveType.DoubleType;
  }

  public static int getRegisterSizeCount(Type sootType) {
    return isWide(sootType) ? 2 : 1;
  }

  public static int getRegisterSizeCount(Collection<Type> sootTypes) {
    return sootTypes.stream().mapToInt(DexUtil::getRegisterSizeCount).sum();
  }

  public static int getRegisterSizeCount(List<Register> registers) {
    // if register type is long or double -> count 2 registers; else 1 register
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
}
