package sootup.apk.backend;

import sootup.core.types.Type;

public class Register {

  private int number;
  private Type type;
  private boolean isTypeGuessed = false;
  private final boolean isParameter;
  private final boolean isTmp;

  protected Register(int number, Type type, boolean isParameter, boolean isTmp) {
    this.number = number;
    this.type = type;
    this.isParameter = isParameter;
    this.isTmp = isTmp;
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
}
