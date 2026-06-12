package de.upb.sootup.concrete.operators;

public class ShortOps {

  // public static void main(String[] args) throws InvocationTargetException, IllegalAccessException {
  // ShortOps ops = new ShortOps();
  // for (Method method : ops.getClass().getMethods()) {
  // if (!method.getName().contains("main")) {
  // System.out.println(method.getName());
  // final O }bject invoke = method.invoke(ops);
  // }
  // }
  //

  public void addition() {
    short a = 5;
    short b = 5;
    short d = (short) (a + b);
    System.out.println(d);
  }

  public void subtraction() {
    short a = 5;
    short b = 5;
    short d = (short) (b - a);
    System.out.println(d);
  }

  public void multiplication() {
    short a = 5;
    short b = 5;
    short d = (short) (b * a);
    System.out.println(d);
  }

  public void division() {
    short a = 5;
    short b = 5;
    short d = (short) (b / a);
    System.out.println(d);
  }

  public void modulus() {
    short a = 5;
    short b = 5;
    short d = (short) (b % a);
    System.out.println(d);
  }

  public void simpleAssignmentOperator() {
    short a = 5;
    short d = a;
    System.out.println(d);
  }

  public void bitwiseAnd() {
    short a = 5;
    short b = 5;
    short d = (short) (a & b);
    System.out.println(d);
  }

  public void bitwiseOr() {
    short a = 5;
    short b = 5;
    short d = (short) (a | b);
    System.out.println(d);
  }

  public void bitwiseXor() {
    short a = 5;
    short b = 5;
    short d = (short) (a ^ b);
    System.out.println(d);
  }

  public void bitwiseCompliment() {
    short a = 5;
    short d = (short) ~a;
    System.out.println(d);
  }

  public void bitwiseLeftShift() {
    short a = 5;
    short d = (short) (a << 2);
    System.out.println(d);
  }

  public void bitwiseRightShift() {
    short a = 5;
    short d = (short) (a >> 2);
    System.out.println(d);
  }

  public void bitwiseRightShiftZerofill() {
    short a = 5;
    short d = (short) (a >>> 2);
    System.out.println(d);
  }

  public void equals() {
    short a = 5;
    short b = 5;
    boolean result = (a == b);
    System.out.println(result);
  }

  public void notEquals() {
    short a = 5;
    short b = 5;
    boolean result = (a != b);
    System.out.println(result);
  }

  public void greateThan() {
    short a = 5;
    short b = 5;
    boolean result = (a > b);
    System.out.println(result);
  }

  public void lessThan() {
    short a = 5;
    short b = 5;
    boolean result = (a < b);
    System.out.println(result);
  }

  public void greaterOrEqualsThan() {
    short a = 5;
    short b = 5;
    boolean result = (a >= b);
    System.out.println(result);
  }

  public void lessOrEqualsThan() {
    short a = 5;
    short b = 5;
    boolean result = (a <= b);
    System.out.println(result);
  }
}
