package de.upb.swt.soot.core.types;

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
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import javax.annotation.Nonnull;

/** Represents Java's primitive types. */
public abstract class PrimitiveType extends Type {

  @Nonnull private static final PrimitiveType BYTE_TYPE = new ByteType();

  @Nonnull private static final PrimitiveType SHORT_TYPE = new ShortType();

  @Nonnull private static final PrimitiveType INT_TYPE = new IntType();

  @Nonnull private static final PrimitiveType LONG_TYPE = new LongType();

  @Nonnull private static final PrimitiveType FLOAT_TYPE = new FloatType();

  @Nonnull private static final PrimitiveType DOUBLE_TYPE = new DoubleType();

  @Nonnull private static final PrimitiveType CHAR_TYPE = new CharType();

  @Nonnull private static final PrimitiveType BOOLEAN_TYPE = new BooleanType();

  @Nonnull
  private static final ImmutableMap<String, PrimitiveType> CACHED_SIGNATURES =
      ImmutableMap.<String, PrimitiveType>builder()
          .put(BYTE_TYPE.getName(), BYTE_TYPE)
          .put(SHORT_TYPE.getName(), SHORT_TYPE)
          .put(INT_TYPE.getName(), INT_TYPE)
          .put(LONG_TYPE.getName(), LONG_TYPE)
          .put(FLOAT_TYPE.getName(), FLOAT_TYPE)
          .put(DOUBLE_TYPE.getName(), DOUBLE_TYPE)
          .put(CHAR_TYPE.getName(), CHAR_TYPE)
          .put(BOOLEAN_TYPE.getName(), BOOLEAN_TYPE)
          .build();

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

  @Override
  @Nonnull
  public String toString() {
    return name;
  }

  @Nonnull
  public static ImmutableCollection<PrimitiveType> all() {
    return CACHED_SIGNATURES.values();
  }

  @Nonnull
  public static PrimitiveType of(@Nonnull String name) {
    return find(name)
        .orElseThrow(() -> new IllegalArgumentException("Name of primitive type not found."));
  }

  @Nonnull
  public static Optional<PrimitiveType> find(@Nonnull String name) {
    return Optional.ofNullable(CACHED_SIGNATURES.get(name));
  }

  public static boolean isIntLikeType(Type t) {
    return t == PrimitiveType.getInt()
        || t == PrimitiveType.getByte()
        || t == PrimitiveType.getShort()
        || t == PrimitiveType.getChar()
        || t == PrimitiveType.getBoolean();
  }

  @Nonnull
  public static PrimitiveType getByte() {
    return BYTE_TYPE;
  }

  @Nonnull
  public static PrimitiveType getShort() {
    return SHORT_TYPE;
  }

  @Nonnull
  public static PrimitiveType getInt() {
    return INT_TYPE;
  }

  @Nonnull
  public static PrimitiveType getLong() {
    return LONG_TYPE;
  }

  @Nonnull
  public static PrimitiveType getFloat() {
    return FLOAT_TYPE;
  }

  @Nonnull
  public static PrimitiveType getDouble() {
    return DOUBLE_TYPE;
  }

  @Nonnull
  public static PrimitiveType getChar() {
    return CHAR_TYPE;
  }

  @Nonnull
  public static PrimitiveType getBoolean() {
    return BOOLEAN_TYPE;
  }

  private static class ByteType extends PrimitiveType {
    private ByteType() {
      super("byte");
    }

    @Override
    <V> V accept(@Nonnull TypeSwitch<V> ts) {
      return ts.caseByteType(this);
    }
  }

  private static class ShortType extends PrimitiveType {
    private ShortType() {
      super("byte");
    }

    @Override
    <V> V accept(@Nonnull TypeSwitch<V> ts) {
      return ts.caseShortType(this);
    }
  }

  private static class IntType extends PrimitiveType {
    public IntType() {
      super("int");
    }

    @Override
    <V> V accept(@Nonnull TypeSwitch<V> ts) {
      return ts.caseShortType(this);
    }
  }

  private static class DoubleType extends PrimitiveType {

    private DoubleType() {
      super("double");
    }

    @Override
    <V> V accept(@Nonnull TypeSwitch<V> ts) {
      return ts.caseDoubleType(this);
    }
  }

  private static class LongType extends PrimitiveType {

    private LongType() {
      super("long");
    }

    @Override
    <V> V accept(@Nonnull TypeSwitch<V> ts) {
      return ts.caseLongType(this);
    }
  }

  private static class FloatType extends PrimitiveType {

    private FloatType() {
      super("float");
    }

    @Override
    <V> V accept(@Nonnull TypeSwitch<V> ts) {
      return ts.caseFloatType(this);
    }
  }

  private static class CharType extends PrimitiveType {
    public CharType() {
      super("char");
    }

    @Override
    <V> V accept(@Nonnull TypeSwitch<V> ts) {
      return ts.caseCharType(this);
    }
  }

  private static class BooleanType extends PrimitiveType {
    private BooleanType() {
      super("boolean");
    }

    @Override
    <V> V accept(@Nonnull TypeSwitch<V> ts) {
      return ts.caseBooleanType(this);
    }
  }
}
