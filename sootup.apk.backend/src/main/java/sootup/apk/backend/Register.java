package sootup.apk.backend;

import sootup.core.types.Type;

public class Register {

  private final int number;
  private final Type type;

  protected Register(int number, Type type) {
    this.number = number;
    this.type = type;
  }

  public int getNumber() {
    return number;
  }

  public Type getType() {
    return type;
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

  public int getSize() {
    return DexUtil.getRegisterSizeCount(type);
  }
}
