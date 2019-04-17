package de.upb.soot.jimple.common.constant;

import de.upb.soot.jimple.visitor.IConstantVisitor;
import de.upb.soot.jimple.visitor.IVisitor;
import de.upb.soot.types.PrimitiveType;
import de.upb.soot.types.Type;

/**
 * BooleanConstant didn't exist in old soot, because in Java byte code boolean values are
 * represented as integer values 1 or 0. However, from the source code we have the information if a
 * constant is boolean or not, adding this class is helpful for setting type of boolean variables.
 *
 * @author Linghui Luo
 */
public class BooleanConstant extends IntConstant {

  /** */
  private static final long serialVersionUID = 6986012843042501546L;

  protected BooleanConstant(int value) {
    super(value);
  }

  public static BooleanConstant getInstance(int value) {
    if (value != 1 && value != 0) {
      throw new RuntimeException("The value of boolean constant can only be 1 or 0");
    }
    return new BooleanConstant(value);
  }

  @Override
  public Type getType() {
    return PrimitiveType.getBoolean();
  }

  @Override
  public void accept(IVisitor sw) {
    ((IConstantVisitor) sw).caseBooleanConstant(this);
  }

  @Override
  public NumericConstant add(NumericConstant c) {
    if (!(c instanceof BooleanConstant)) {
      throw new IllegalArgumentException("BooleanConstant expected");
    }
    return BooleanConstant.getInstance(this.value + ((BooleanConstant) c).value);
  }

  @Override
  public NumericConstant subtract(NumericConstant c) {
    if (!(c instanceof BooleanConstant)) {
      throw new IllegalArgumentException("BooleanConstant expected");
    }
    return BooleanConstant.getInstance(this.value - ((BooleanConstant) c).value);
  }

  @Override
  public NumericConstant multiply(NumericConstant c) {
    if (!(c instanceof BooleanConstant)) {
      throw new IllegalArgumentException("BooleanConstant expected");
    }
    return BooleanConstant.getInstance(this.value * ((BooleanConstant) c).value);
  }

  @Override
  public NumericConstant divide(NumericConstant c) {
    if (!(c instanceof BooleanConstant)) {
      throw new IllegalArgumentException("BooleanConstant expected");
    }
    return BooleanConstant.getInstance(this.value / ((BooleanConstant) c).value);
  }

  @Override
  public NumericConstant remainder(NumericConstant c) {
    if (!(c instanceof BooleanConstant)) {
      throw new IllegalArgumentException("BooleanConstant expected");
    }
    return BooleanConstant.getInstance(this.value % ((BooleanConstant) c).value);
  }

  @Override
  public NumericConstant equalEqual(NumericConstant c) {
    if (!(c instanceof BooleanConstant)) {
      throw new IllegalArgumentException("BooleanConstant expected");
    }
    return BooleanConstant.getInstance((this.value == ((BooleanConstant) c).value) ? 1 : 0);
  }

  @Override
  public NumericConstant notEqual(NumericConstant c) {
    if (!(c instanceof BooleanConstant)) {
      throw new IllegalArgumentException("BooleanConstant expected");
    }
    return BooleanConstant.getInstance((this.value != ((BooleanConstant) c).value) ? 1 : 0);
  }

  @Override
  public NumericConstant lessThan(NumericConstant c) {
    if (!(c instanceof BooleanConstant)) {
      throw new IllegalArgumentException("BooleanConstant expected");
    }
    return BooleanConstant.getInstance((this.value < ((BooleanConstant) c).value) ? 1 : 0);
  }

  @Override
  public NumericConstant lessThanOrEqual(NumericConstant c) {
    if (!(c instanceof BooleanConstant)) {
      throw new IllegalArgumentException("BooleanConstant expected");
    }
    return BooleanConstant.getInstance((this.value <= ((BooleanConstant) c).value) ? 1 : 0);
  }

  @Override
  public NumericConstant greaterThan(NumericConstant c) {
    if (!(c instanceof BooleanConstant)) {
      throw new IllegalArgumentException("BooleanConstant expected");
    }
    return BooleanConstant.getInstance((this.value > ((BooleanConstant) c).value) ? 1 : 0);
  }

  @Override
  public NumericConstant greaterThanOrEqual(NumericConstant c) {
    if (!(c instanceof BooleanConstant)) {
      throw new IllegalArgumentException("BooleanConstant expected");
    }
    return BooleanConstant.getInstance((this.value >= ((BooleanConstant) c).value) ? 1 : 0);
  }

  @Override
  public NumericConstant negate() {
    return BooleanConstant.getInstance(-(this.value));
  }

  @Override
  public ArithmeticConstant and(ArithmeticConstant c) {
    if (!(c instanceof BooleanConstant)) {
      throw new IllegalArgumentException("BooleanConstant expected");
    }
    return BooleanConstant.getInstance(this.value & ((BooleanConstant) c).value);
  }

  @Override
  public ArithmeticConstant or(ArithmeticConstant c) {
    if (!(c instanceof BooleanConstant)) {
      throw new IllegalArgumentException("BooleanConstant expected");
    }
    return BooleanConstant.getInstance(this.value | ((BooleanConstant) c).value);
  }

  @Override
  public ArithmeticConstant xor(ArithmeticConstant c) {
    if (!(c instanceof BooleanConstant)) {
      throw new IllegalArgumentException("BooleanConstant expected");
    }
    return BooleanConstant.getInstance(this.value ^ ((BooleanConstant) c).value);
  }

  @Override
  public ArithmeticConstant shiftLeft(ArithmeticConstant c) {
    if (!(c instanceof BooleanConstant)) {
      throw new IllegalArgumentException("BooleanConstant expected");
    }
    return BooleanConstant.getInstance(this.value << ((BooleanConstant) c).value);
  }

  @Override
  public ArithmeticConstant shiftRight(ArithmeticConstant c) {
    if (!(c instanceof BooleanConstant)) {
      throw new IllegalArgumentException("BooleanConstant expected");
    }
    return BooleanConstant.getInstance(this.value >> ((BooleanConstant) c).value);
  }

  @Override
  public ArithmeticConstant unsignedShiftRight(ArithmeticConstant c) {
    if (!(c instanceof BooleanConstant)) {
      throw new IllegalArgumentException("BooleanConstant expected");
    }
    return BooleanConstant.getInstance(this.value >>> ((BooleanConstant) c).value);
  }
}
