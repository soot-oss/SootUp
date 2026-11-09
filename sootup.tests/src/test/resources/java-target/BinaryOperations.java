public class BinaryOperations {
  public byte addByte(byte a, byte b) {
    return (byte) (a + b);
  }

  public double addDouble(double a, float b) {
    return a + b;
  }

  public double mulDouble(double a, double b) {
    return a * b;
  }

  public char subChar(char a, char b) {
    return (char) (a - b);
  }

  public short mulShort(short a, short b) {
    return (short) (a * b);
  }

  public int divInt(int a, int b) {
    return a / b;
  }

  public char modChar(char a, char b) {
    return (char) (a % b);
  }

  public short incShort(short a) {
    return a++;
  }

  public int decInt(int a) {
    return --a;
  }

  public long orLong(long a, long b) {
    return a | b;
  }

  public int xorInt(int a, int b) {
    return a ^ b;
  }

  public char andChar(char a, char b) {
    return (char) (a & b);
  }

  public byte lshiftByte(byte a) {
    return (byte) (a << 2);
  }

  public short rshiftShort(short a, int b) {
    return (short) (a >> b);
  }

  public long negLong(long a) {
    return ~a;
  }

  public int zeroFillRshiftInt(int a, int b) {
    return a >>> b;
  }

  public boolean logicalAnd(boolean a, boolean b) {
    return a && b;
  }

  public boolean logicalOr(boolean a, boolean b) {
    return a || b;
  }

  public boolean not(boolean a) {
    return !a;
  }

  public boolean equal(int a, int b) {
    return a == b;
  }

  public boolean notEqual(float a, float b) {
    return a != b;
  }

  public boolean greater(double a, double b) {
    return a > b;
  }

  public boolean smaller(long a, long b) {
    return a < b;
  }

  public boolean greaterEqual(char a, char b) {
    return a >= b;
  }

  public boolean smallerEqual(byte a, byte b) {
    return a <= b;
  }
}