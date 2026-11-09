package de.upb.sootup.concrete.operators;

public class ByteOps {

  public void addition() {
    byte a = 5;
    byte b = 5;
    byte d = (byte) (a + b);
    System.out.println(d);
  }

  public void subtraction() {
    byte a = 5;
    byte b = 5;
    byte d = (byte) (b - a);
    System.out.println(d);
  }

  public void multiplication() {
    byte a = 5;
    byte b = 5;
    byte d = (byte) (b * a);
    System.out.println(d);
  }

  public void division() {
    byte a = 5;
    byte b = 5;
    byte d = (byte) (b / a);
    System.out.println(d);
  }

  public void modulus() {
    byte a = 5;
    byte b = 5;
    byte d = (byte) (b % a);
    System.out.println(d);
  }

  public void simpleAssignmentOperator() {
    byte a = 5;
    byte d = a;
    System.out.println(d);
  }

  public void bitwiseAnd() {
    byte a = 5;
    byte b = 5;
    byte d = (byte) (a & b);
    System.out.println(d);
  }

  public void bitwiseOr() {
    byte a = 5;
    byte b = 5;
    byte d = (byte) (a | b);
    System.out.println(d);
  }

  public void bitwiseXor() {
    byte a = 5;
    byte b = 5;
    byte d = (byte) (a ^ b);
    System.out.println(d);
  }

  public void bitwiseCompliment() {
    byte a = 5;
    byte d = (byte) ~a;
    System.out.println(d);
  }

  public void bitwiseLeftShift() {
    byte a = 5;
    byte d = (byte) (a << 2);
    System.out.println(d);
  }

  public void bitwiseRightShift() {
    byte a = 5;
    byte d = (byte) (a >> 2);
    System.out.println(d);
  }

  public void bitwiseRightShiftZerofill() {
    byte a = 5;
    byte d = (byte) (a >>> 2);
    System.out.println(d);
  }

  public void equals() {
    byte a = 5;
    byte b = 5;
    boolean result = (a == b);
    System.out.println(result);
  }

  public void notEquals() {
    byte a = 5;
    byte b = 5;
    boolean result = (a != b);
    System.out.println(result);
  }

  public void greateThan() {
    byte a = 5;
    byte b = 5;
    boolean result = (a > b);
    System.out.println(result);
  }

  public void lessThan() {
    byte a = 5;
    byte b = 5;
    boolean result = (a < b);
    System.out.println(result);
  }

  public void greaterOrEqualsThan() {
    byte a = 5;
    byte b = 5;
    boolean result = (a >= b);
    System.out.println(result);
  }

  public void lessOrEqualsThan() {
    byte a = 5;
    byte b = 5;
    boolean result = (a <= b);
    System.out.println(result);
  }
}
