package de.upb.sootup.concrete.operators;

public class DoubleOps {

  // public static void main(String[] args) throws InvocationTargetException, IllegalAccessException {
  // DoubleOps ops = new DoubleOps();
  // for (Method method : ops.getClass().getMethods()) {
  // if (!method.getName().contains("main")) {
  // final Object invoke = method.invoke(ops);
  // System.out.println(method.getName());
  // }
  // }
  // }

  public void addition() {
    double a = 16777217d;
    double b = 5d;
    double d = a + b;
    System.out.println(d);
  }

  public void subtraction() {
    double a = 16777217d;
    double b = 5d;
    double d = b - a;
    System.out.println(d);
  }

  public void multiplication() {
    double a = 16777217d;
    double b = 5d;
    double d = b * a;
    System.out.println(d);
  }

  public void division() {
    double a = 16777217d;
    double b = 5d;
    double d = b / a;
    System.out.println(d);
  }

  public void modulus() {
    double a = 16777217d;
    double b = 5d;
    double d = b % a;
    System.out.println(d);
  }

  public void simpleAssignmentOperator() {
    double a = 16777217d;
    double d = a;
    System.out.println(d);
  }

  public void equals() {
    double a = 16777217d;
    double b = 5d;
    boolean result = (a == b);
    System.out.println(result);
  }

  public void notEquals() {
    double a = 16777217d;
    double b = 5d;
    boolean result = (a != b);
    System.out.println(result);
  }

  public void greateThan() {
    double a = 16777217d;
    double b = 5d;
    boolean result = (a > b);
    System.out.println(result);
  }

  public void lessThan() {
    double a = 16777217d;
    double b = 5d;
    boolean result = (a < b);
    System.out.println(result);
  }

  public void greaterOrEqualsThan() {
    double a = 16777217d;
    double b = 5d;
    boolean result = (a >= b);
    System.out.println(result);
  }

  public void lessOrEqualsThan() {
    double a = 16777217d;
    double b = 5d;
    boolean result = (a <= b);
    System.out.println(result);
  }
}
