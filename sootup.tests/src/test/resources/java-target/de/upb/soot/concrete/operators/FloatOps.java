package de.upb.sootup.concrete.operators;

public class FloatOps {

  // public static void main(String[] args) throws InvocationTargetException, IllegalAccessException {
  // FloatOps ops = new FloatOps();
  // for (Method method : ops.getClass().getMethods()) {
  // if (!method.getName().contains("main")) {
  // final Object invoke = method.invoke(ops);
  // System.out.println(method.getName());
  // }
  // }
  // }

  public void addition() {
    float a = 5.5f;
    float b = 5f;
    float d = a + b;
    System.out.println(d);
  }

  public void subtraction() {
    float a = 5.5f;
    float b = 5f;
    float d = b - a;
    System.out.println(d);
  }

  public void multiplication() {
    float a = 5.5f;
    float b = 5f;
    float d = b * a;
    System.out.println(d);
  }

  public void division() {
    float a = 5.5f;
    float b = 5f;
    float d = b / a;
    System.out.println(d);
  }

  public void modulus() {
    float a = 5.5f;
    float b = 5f;
    float d = b % a;
    System.out.println(d);
  }

  public void simpleAssignmentOperator() {
    float a = 5.5f;
    float d = a;
    System.out.println(d);
  }

  public void equals() {
    float a = 5.5f;
    float b = 5f;
    boolean result = (a == b);
    System.out.println(result);
  }

  public void notEquals() {
    float a = 5.5f;
    float b = 5f;
    boolean result = (a != b);
    System.out.println(result);
  }

  public void greateThan() {
    float a = 5.5f;
    float b = 5f;
    boolean result = (a > b);
    System.out.println(result);
  }

  public void lessThan() {
    float a = 5.5f;
    float b = 5f;
    boolean result = (a < b);
    System.out.println(result);
  }

  public void greaterOrEqualsThan() {
    float a = 5.5f;
    float b = 5f;
    boolean result = (a >= b);
    System.out.println(result);
  }

  public void lessOrEqualsThan() {
    float a = 5.5f;
    float b = 5f;
    boolean result = (a <= b);
    System.out.println(result);
  }
}
