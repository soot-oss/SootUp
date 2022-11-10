package de.upb.sootup.concrete.operators;

public class LongOps {

  //
  // public static void main(String[] args) throws InvocationTargetException, IllegalAccessException {
  // LongOps ops = new LongOps();
  // for (Method method : ops.getClass().getMethods()) {
  // if (!method.getName().contains("main")) {
  // System.out.println(method.getName());
  // final Object invoke = method.invoke(ops);
  // }
  // }
  // }

  public void addition() {
    long a = 2147483648L;
    long b = 5L;
    long d = a + b;
    System.out.println(d);
  }

  public void subtraction() {
    long a = 2147483648L;
    long b = 5L;
    long d = b - a;
    System.out.println(d);
  }

  public void multiplication() {
    long a = 2147483648L;
    long b = 5L;
    long d = b * a;
    System.out.println(d);
  }

  public void division() {
    long a = 2147483648L;
    long b = 5L;
    long d = b / a;
    System.out.println(d);
  }

  public void modulus() {
    long a = 2147483648L;
    long b = 5L;
    long d = b % a;
    System.out.println(d);
  }

  public void simpleAssignmentOperator() {
    long a = 2147483648L;
    long d = a;
    System.out.println(d);
  }

  public void bitwiseAnd() {
    long a = 2147483648L;
    long b = 5L;
    long d = a & b;
    System.out.println(d);
  }

  public void bitwiseOr() {
    long a = 2147483648L;
    long b = 5L;
    long d = a | b;
    System.out.println(d);
  }

  public void bitwiseXor() {
    long a = 2147483648L;
    long b = 5L;
    long d = a ^ b;
    System.out.println(d);
  }

  public void bitwiseCompliment() {
    long a = 2147483648L;
    long d = ~a;
    System.out.println(d);
  }

  public void bitwiseLeftShift() {
    long a = 2147483648L;
    long d = a << 2;
    System.out.println(d);
  }

  public void bitwiseRightShift() {
    long a = 2147483648L;
    long d = a >> 2;
    System.out.println(d);
  }

  public void bitwiseRightShiftZerofill() {
    long a = 2147483648L;
    long d = a >>> 2;
    System.out.println(d);
  }

  public void equals() {
    long a = 2147483648L;
    long b = 5L;
    boolean result = (a == b);
    System.out.println(result);
  }

  public void notEquals() {
    long a = 2147483648L;
    long b = 5L;
    boolean result = (a != b);
    System.out.println(result);
  }

  public void greateThan() {
    long a = 2147483648L;
    long b = 5L;
    boolean result = (a > b);
    System.out.println(result);
  }

  public void lessThan() {
    long a = 2147483648L;
    long b = 5L;
    boolean result = (a < b);
    System.out.println(result);
  }

  public void greaterOrEqualsThan() {
    long a = 2147483648L;
    long b = 5L;
    boolean result = (a >= b);
    System.out.println(result);
  }

  public void lessOrEqualsThan() {
    long a = 2147483648L;
    long b = 5L;
    boolean result = (a <= b);
    System.out.println(result);
  }
}
