package sootup.core.types;

/*-
 * #%L
 * Soot
 * %%
 * Copyright (C) 2019-2020 Christian Brüggemann, Markus Schmidt
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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import sootup.core.jimple.visitor.TypeVisitor;

/** Represents Java's primitive types. */
public abstract class PrimitiveType extends Type {

  /**
   * Signatures of primitive types are unique and should not be created from the outside, thus the
   * constructor is private.
   *
   * @param name the primitive's name
   */
  private PrimitiveType(@Nonnull String name) {
    this.name = name;
  }

  @Nonnull private final String name;

  /**
   * Gets the primitive type's name.
   *
   * @return The value to get.
   */
  @Nonnull
  public String getName() {
    return name;
  }

  @Nonnull
  public abstract String getBoxedName();

  @Override
  @Nonnull
  public String toString() {
    return name;
  }

  private static final Map<Class<? extends PrimitiveType>, Set<Class<? extends PrimitiveType>>>
      implicitConversionMap =
          ImmutableMap
              .<Class<? extends PrimitiveType>, Set<Class<? extends PrimitiveType>>>builder()
              .put(
                  ByteType.class,
                  ImmutableSet.of(
                      ShortType.class,
                      IntType.class,
                      LongType.class,
                      FloatType.class,
                      DoubleType.class))
              .put(
                  ShortType.class,
                  ImmutableSet.of(IntType.class, LongType.class, FloatType.class, DoubleType.class))
              .put(
                  CharType.class,
                  ImmutableSet.of(IntType.class, LongType.class, FloatType.class, DoubleType.class))
              .put(
                  IntType.class, ImmutableSet.of(LongType.class, FloatType.class, DoubleType.class))
              .put(LongType.class, ImmutableSet.of(FloatType.class, DoubleType.class))
              .put(FloatType.class, ImmutableSet.of(DoubleType.class))
              .build();

  /**
   * @param fromType e.g. the method argument
   * @param toType e.g. the method parameter
   * @return true if type conversion is possible
   */
  public static boolean isImplicitlyConvertibleTo(
      @Nonnull PrimitiveType fromType, @Nonnull PrimitiveType toType) {
    Class<? extends PrimitiveType> fromTypeClass = fromType.getClass();
    Class<? extends PrimitiveType> toTypeClass = toType.getClass();
    return implicitConversionMap.containsKey(fromTypeClass)
            && implicitConversionMap.get(fromTypeClass).contains(toTypeClass)
        || IntType.class.isAssignableFrom(fromTypeClass)
            && implicitConversionMap.get(IntType.class).contains(toTypeClass);
  }

  @Nonnull
  public static ByteType getByte() {
    return ByteType.getInstance();
  }

  @Nonnull
  public static ShortType getShort() {
    return ShortType.getInstance();
  }

  @Nonnull
  public static IntType getInt() {
    return IntType.getInstance();
  }

  @Nonnull
  public static LongType getLong() {
    return LongType.getInstance();
  }

  @Nonnull
  public static FloatType getFloat() {
    return FloatType.getInstance();
  }

  @Nonnull
  public static DoubleType getDouble() {
    return DoubleType.getInstance();
  }

  @Nonnull
  public static CharType getChar() {
    return CharType.getInstance();
  }

  @Nonnull
  public static BooleanType getBoolean() {
    return BooleanType.getInstance();
  }

  public static class ByteType extends PrimitiveType.IntType {
    private static final ByteType INSTANCE = new ByteType();

    private ByteType() {
      super("byte");
    }

    public static ByteType getInstance() {
      return INSTANCE;
    }

    @Nonnull
    @Override
    public String getBoxedName() {
      return "Byte";
    }

    @Override
    public <V extends TypeVisitor> V accept(@Nonnull V v) {
      v.caseByteType();
      return v;
    }
  }

  public static class ShortType extends PrimitiveType.IntType {
    private static final ShortType INSTANCE = new ShortType();

    private ShortType() {
      super("short");
    }

    public static ShortType getInstance() {
      return INSTANCE;
    }

    @Nonnull
    @Override
    public String getBoxedName() {
      return "Short";
    }

    @Override
    public <V extends TypeVisitor> V accept(@Nonnull V v) {
      v.caseShortType();
      return v;
    }
  }

  public static class IntType extends PrimitiveType {
    private static final IntType INSTANCE = new IntType();

    public IntType() {
      super("int");
    }

    protected IntType(@Nonnull String name) {
      super(name);
    }

    public static IntType getInstance() {
      return INSTANCE;
    }

    @Nonnull
    @Override
    public String getBoxedName() {
      return "Integer";
    }

    @Override
    public <V extends TypeVisitor> V accept(@Nonnull V v) {
      v.caseIntType();
      return v;
    }
  }

  public static class DoubleType extends PrimitiveType {
    private static final DoubleType INSTANCE = new DoubleType();

    private DoubleType() {
      super("double");
    }

    public static DoubleType getInstance() {
      return INSTANCE;
    }

    @Nonnull
    @Override
    public String getBoxedName() {
      return "Double";
    }

    @Override
    public <V extends TypeVisitor> V accept(@Nonnull V v) {
      v.caseDoubleType();
      return v;
    }
  }

  public static class LongType extends PrimitiveType {
    private static final LongType INSTANCE = new LongType();

    private LongType() {
      super("long");
    }

    public static LongType getInstance() {
      return INSTANCE;
    }

    @Nonnull
    @Override
    public String getBoxedName() {
      return "Long";
    }

    @Override
    public <V extends TypeVisitor> V accept(@Nonnull V v) {
      v.caseLongType();
      return v;
    }
  }

  public static class FloatType extends PrimitiveType {
    private static final FloatType INSTANCE = new FloatType();

    private FloatType() {
      super("float");
    }

    public static FloatType getInstance() {
      return INSTANCE;
    }

    @Nonnull
    @Override
    public String getBoxedName() {
      return "Float";
    }

    @Override
    public <V extends TypeVisitor> V accept(@Nonnull V v) {
      v.caseFloatType();
      return v;
    }
  }

  public static class CharType extends PrimitiveType.IntType {
    private static final CharType INSTANCE = new CharType();

    private CharType() {
      super("char");
    }

    public static CharType getInstance() {
      return INSTANCE;
    }

    @Nonnull
    @Override
    public String getBoxedName() {
      return "Character";
    }

    @Override
    public <V extends TypeVisitor> V accept(@Nonnull V v) {
      v.caseCharType();
      return v;
    }
  }

  public static class BooleanType extends PrimitiveType.IntType {
    private static final BooleanType INSTANCE = new BooleanType();

    private BooleanType() {
      super("boolean");
    }

    protected BooleanType(@Nonnull String name) {
      super(name);
    }

    public static BooleanType getInstance() {
      return INSTANCE;
    }

    @Nonnull
    @Override
    public String getBoxedName() {
      return "Boolean";
    }

    @Override
    public <V extends TypeVisitor> V accept(@Nonnull V v) {
      v.caseBooleanType();
      return v;
    }
  }
}
