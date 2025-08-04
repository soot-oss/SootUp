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

import java.util.Optional;
import org.jspecify.annotations.NonNull;
import sootup.core.jimple.visitor.TypeVisitor;

/** Represents Java's primitive types. */
public abstract class PrimitiveType extends Type {

  /**
   * Signatures of primitive types are unique and should not be created from the outside, thus the
   * constructor is private.
   *
   * @param name the primitive's name
   */
  private PrimitiveType(@NonNull String name) {
    this.name = name;
  }

  @NonNull private final String name;

  /**
   * Gets the primitive type's name.
   *
   * @return The value to get.
   */
  @NonNull
  public String getName() {
    return name;
  }

  @Override
  @NonNull
  public String toString() {
    return name;
  }

  @NonNull
  public static ByteType getByte() {
    return ByteType.getInstance();
  }

  @NonNull
  public static ShortType getShort() {
    return ShortType.getInstance();
  }

  @NonNull
  public static IntType getInt() {
    return IntType.getInstance();
  }

  @NonNull
  public static LongType getLong() {
    return LongType.getInstance();
  }

  @NonNull
  public static FloatType getFloat() {
    return FloatType.getInstance();
  }

  @NonNull
  public static DoubleType getDouble() {
    return DoubleType.getInstance();
  }

  @NonNull
  public static CharType getChar() {
    return CharType.getInstance();
  }

  @NonNull
  public static BooleanType getBoolean() {
    return BooleanType.getInstance();
  }

  protected boolean isByteType() {
    return false;
  }

  protected boolean isShortType() {
    return false;
  }

  protected boolean isIntType() {
    return false;
  }

  protected boolean isDoubleType() {
    return false;
  }

  protected boolean isLongType() {
    return false;
  }

  protected boolean isFloatType() {
    return false;
  }

  protected boolean isCharType() {
    return false;
  }

  protected boolean isBooleanType() {
    return false;
  }

  protected boolean isInteger127Type() {
    return false;
  }

  protected boolean isInteger1Type() {
    return false;
  }

  protected boolean isInteger3276Type() {
    return false;
  }

  protected PrimitiveType.ByteType asByteType() {
    return null;
  }

  protected PrimitiveType.ShortType asShortType() {
    return null;
  }

  protected PrimitiveType.IntType asIntType() {
    return null;
  }

  protected PrimitiveType.DoubleType asDoubleType() {
    return null;
  }

  protected PrimitiveType.LongType asLongType() {
    return null;
  }

  protected PrimitiveType.FloatType asFloatType() {
    return null;
  }

  protected PrimitiveType.CharType asCharType() {
    return null;
  }

  protected PrimitiveType.BooleanType asBooleanType() {
    return null;
  }

  protected PrimitiveType.IntType asInteger127Type() {
    return null;
  }

  protected PrimitiveType.BooleanType asInteger1Type() {
    return null;
  }

  protected PrimitiveType.IntType asInteger3276Type() {
    return null;
  }

  protected Optional<PrimitiveType.ByteType> toByteType() {
    return Optional.empty();
  }

  protected Optional<PrimitiveType.ShortType> toShortType() {
    return Optional.empty();
  }

  protected Optional<PrimitiveType.IntType> toIntType() {
    return Optional.empty();
  }

  protected Optional<PrimitiveType.DoubleType> toDoubleType() {
    return Optional.empty();
  }

  protected Optional<PrimitiveType.LongType> toLongType() {
    return Optional.empty();
  }

  protected Optional<PrimitiveType.FloatType> toFloatType() {
    return Optional.empty();
  }

  protected Optional<PrimitiveType.CharType> toCharType() {
    return Optional.empty();
  }

  protected Optional<PrimitiveType.BooleanType> toBooleanType() {
    return Optional.empty();
  }

  protected Optional<PrimitiveType.IntType> toInteger127Type() {
    return Optional.empty();
  }

  protected Optional<PrimitiveType.BooleanType> toInteger1Type() {
    return Optional.empty();
  }

  protected Optional<PrimitiveType.IntType> toInteger3276Type() {
    return Optional.empty();
  }

  public static class ByteType extends PrimitiveType.IntType {
    private static final ByteType INSTANCE = new ByteType();

    private ByteType() {
      super("byte");
    }

    public static ByteType getInstance() {
      return INSTANCE;
    }

    @Override
    public <V extends TypeVisitor> V accept(@NonNull V v) {
      v.caseByteType();
      return v;
    }

    @Override
    protected boolean isByteType() {
      return true;
    }

    @Override
    protected ByteType asByteType() {
      return this;
    }

    @Override
    protected Optional<ByteType> toByteType() {
      return Optional.of(this);
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

    @Override
    public <V extends TypeVisitor> V accept(@NonNull V v) {
      v.caseShortType();
      return v;
    }

    @Override
    protected boolean isShortType() {
      return true;
    }

    @Override
    protected ShortType asShortType() {
      return this;
    }

    @Override
    protected Optional<ShortType> toShortType() {
      return Optional.of(this);
    }
  }

  public static class IntType extends PrimitiveType {
    private static final IntType INSTANCE = new IntType();

    public IntType() {
      super("int");
    }

    protected IntType(@NonNull String name) {
      super(name);
    }

    @Override
    protected boolean isIntType() {
      return true;
    }

    @Override
    protected IntType asIntType() {
      return this;
    }

    @Override
    protected Optional<IntType> toIntType() {
      return Optional.of(this);
    }

    public static IntType getInstance() {
      return INSTANCE;
    }

    @Override
    public <V extends TypeVisitor> V accept(@NonNull V v) {
      v.caseIntType();
      return v;
    }

    @Override
    protected boolean isPrimitiveType() {
      return true;
    }

    @Override
    protected PrimitiveType asPrimitiveType() {
      return this;
    }

    @Override
    protected Optional<PrimitiveType> toPrimitiveType() {
      return Optional.of(this);
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

    @Override
    public <V extends TypeVisitor> V accept(@NonNull V v) {
      v.caseDoubleType();
      return v;
    }

    @Override
    protected boolean isDoubleType() {
      return true;
    }

    @Override
    protected DoubleType asDoubleType() {
      return this;
    }

    @Override
    protected Optional<DoubleType> toDoubleType() {
      return Optional.of(this);
    }

    @Override
    protected boolean isPrimitiveType() {
      return true;
    }

    @Override
    protected PrimitiveType asPrimitiveType() {
      return this;
    }

    @Override
    protected Optional<PrimitiveType> toPrimitiveType() {
      return Optional.of(this);
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

    @Override
    public <V extends TypeVisitor> V accept(@NonNull V v) {
      v.caseLongType();
      return v;
    }

    @Override
    protected boolean isLongType() {
      return true;
    }

    @Override
    protected LongType asLongType() {
      return this;
    }

    @Override
    protected Optional<LongType> toLongType() {
      return Optional.of(this);
    }

    @Override
    protected boolean isPrimitiveType() {
      return true;
    }

    @Override
    protected PrimitiveType asPrimitiveType() {
      return this;
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

    @Override
    public <V extends TypeVisitor> V accept(@NonNull V v) {
      v.caseFloatType();
      return v;
    }

    @Override
    protected boolean isFloatType() {
      return true;
    }

    @Override
    protected FloatType asFloatType() {
      return this;
    }

    @Override
    protected Optional<FloatType> toFloatType() {
      return Optional.of(this);
    }

    @Override
    protected boolean isPrimitiveType() {
      return true;
    }

    @Override
    protected PrimitiveType asPrimitiveType() {
      return this;
    }

    @Override
    protected Optional<PrimitiveType> toPrimitiveType() {
      return Optional.of(this);
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

    @Override
    public <V extends TypeVisitor> V accept(@NonNull V v) {
      v.caseCharType();
      return v;
    }

    @Override
    protected boolean isCharType() {
      return true;
    }

    @Override
    protected CharType asCharType() {
      return this;
    }

    @Override
    protected Optional<CharType> toCharType() {
      return Optional.of(this);
    }
  }

  public static class BooleanType extends PrimitiveType.IntType {
    private static final BooleanType INSTANCE = new BooleanType();

    private BooleanType() {
      super("boolean");
    }

    protected BooleanType(@NonNull String name) {
      super(name);
    }

    public static BooleanType getInstance() {
      return INSTANCE;
    }

    @Override
    public <V extends TypeVisitor> V accept(@NonNull V v) {
      v.caseBooleanType();
      return v;
    }

    @Override
    protected boolean isBooleanType() {
      return true;
    }

    @Override
    protected BooleanType asBooleanType() {
      return this;
    }

    @Override
    protected Optional<BooleanType> toBooleanType() {
      return Optional.of(this);
    }
  }
}
