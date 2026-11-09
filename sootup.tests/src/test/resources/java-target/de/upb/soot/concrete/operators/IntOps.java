package de.upb.sootup.concrete.operators;

public class IntOps {
  /*
   * public static void main(String[] args) throws InvocationTargetException, IllegalAccessException { IntOps ops = new
   * IntOps(); for (Method method : IntOps.class.getMethods()) { if (!method.getName().contains("main")) { final Object
   * invoke = method.invoke(ops); System.out.println(method.getName()); } } }
   */

  public void addition() {
    int a = 5;
    int b = 5;
    int d = a + b;
    System.out.println(d);
  }

  public void subtraction() {
    int a = 5;
    int b = 5;
    int d = b - a;
    System.out.println(d);
  }

  public void multiplication() {
    int a = 5;
    int b = 5;
    int d = b * a;
    System.out.println(d);
  }

  public void division() {
    int a = 5;
    int b = 5;
    int d = b / a;
    System.out.println(d);
  }

  public void modulus() {
    int a = 5;
    int b = 5;
    int d = b % a;
    System.out.println(d);
  }

  public void simpleAssignmentOperator() {
    int a = 5;
    int d = a;
    System.out.println(d);
  }

  public void bitwiseAnd() {
    int a = 5;
    int b = 5;
    int d = a & b;
    System.out.println(d);
  }

  public void bitwiseOr() {
    int a = 5;
    int b = 5;
    int d = a | b;
    System.out.println(d);
  }

  public void bitwiseXor() {
    int a = 5;
    int b = 5;
    int d = a ^ b;
    System.out.println(d);
  }

  public void bitwiseCompliment() {
    int a = 5;
    int d = ~a;
    System.out.println(d);
  }

  public void bitwiseLeftShift() {
    int a = 5;
    int d = a << 2;
    System.out.println(d);
  }

  public void bitwiseRightShift() {
    int a = 5;
    int d = a >> 2;
    System.out.println(d);
  }

  public void bitwiseRightShiftZerofill() {
    int a = 5;
    int d = a >>> 2;
    System.out.println(d);
  }

  public void equals() {
    int a = 5;
    int b = 5;
    boolean result = (a == b);
    System.out.println(result);
  }

  public void notEquals() {
    int a = 5;
    int b = 5;
    boolean result = (a != b);
    System.out.println(result);
  }

  public void greateThan() {
    int a = 5;
    int b = 5;
    boolean result = (a > b);
    System.out.println(result);
  }

  public void lessThan() {
    int a = 5;
    int b = 5;
    boolean result = (a < b);
    System.out.println(result);
  }

  public void greaterOrEqualsThan() {
    int a = 5;
    int b = 5;
    boolean result = (a >= b);
    System.out.println(result);
  }

  public void lessOrEqualsThan() {
    int a = 5;
    int b = 5;
    boolean result = (a <= b);
    System.out.println(result);
  }
}
