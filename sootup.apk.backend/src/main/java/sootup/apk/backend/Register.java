package sootup.apk.backend;

import java.util.ArrayList;
import java.util.List;
import sootup.apk.backend.instructions.AbstractInstruction;
import sootup.core.types.Type;

public class Register {

  private int number;
  private Type type;
  private boolean isTypeGuessed = false;
  private boolean potentialNullValue = false;
  private final boolean isParameter;
  private final boolean isTmp;

  private final List<AbstractInstruction> defs;
  private final List<AbstractInstruction> uses;

  protected Register(int number, Type type, boolean isParameter, boolean isTmp) {
    this.number = number;
    this.type = type;
    this.isParameter = isParameter;
    this.isTmp = isTmp;
    this.defs = new ArrayList<>();
    this.uses = new ArrayList<>();
  }

  public int getNumber() {
    return number;
  }

  public void setNumber(int number) {
    this.number = number;
  }

  public Type getType() {
    return type;
  }

  public boolean isTypeGuessed() {
    return isTypeGuessed;
  }

  public void setIsTypeGuessed(boolean isTypeGuessed) {
    this.isTypeGuessed = isTypeGuessed;
  }

  public boolean isPotentialNullValue() {
    return potentialNullValue;
  }

  public void setIsPotentialNullValue(boolean isPotentialNullValue) {
    this.potentialNullValue = isPotentialNullValue;
  }

  public void setType(Type type) {
    this.type = type;
  }

  public boolean is4BitRegister() {
    return this.number >= 0 && this.number <= 15;
  }

  public boolean is8BitRegister() {
    return this.number >= 0 && this.number <= 255;
  }

  public boolean isWide() {
    return DexUtil.isWide(type);
  }

  public boolean isParameter() {
    return isParameter;
  }

  public boolean isTmp() {
    return isTmp;
  }

  public int getSize() {
    return DexUtil.getRegisterSizeCount(type);
  }

  public List<AbstractInstruction> getDefs() {
    return defs;
  }

  public List<AbstractInstruction> getUses() {
    return uses;
  }

  public void addDef(AbstractInstruction instruction) {
    this.defs.add(instruction);
  }

  public void addUse(AbstractInstruction instruction) {
    this.uses.add(instruction);
  }

  private static boolean fitsInto(int regNumber, int maxNumber, boolean isWide) {
    if (isWide) {
      return regNumber >= 0 && regNumber < maxNumber;
    }
    return regNumber >= 0 && regNumber <= maxNumber;
  }

  private boolean fitsInto(int maxNumber) {
    return fitsInto(number, maxNumber, isWide());
  }

  public boolean fitsUnconstrained() {
    return fitsInto(65535);
  }

  public boolean fitsShort() {
    return fitsInto(255);
  }

  public boolean fitsByte() {
    return fitsInto(15);
  }

  public static boolean fitsUnconstrained(int regNumber, boolean isWide) {
    return fitsInto(regNumber, 65535, isWide);
  }

  @Override
  public Register clone() {
    return new Register(this.number, this.type, this.isParameter, this.isTmp);
  }
}
