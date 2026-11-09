package de.upb.sootup.concrete.operators;

/**
 * @author Manuel Benz created on 11.07.18
 */
public class MiscOps {

  public void instanceofOperator() {
    String name = "Java";
    boolean result = name instanceof String;
    System.out.println(result);
  }

  public void instanceofOperator2() {
    A a = new A();
    boolean result = a instanceof A;
    System.out.println(result);
  }

  public void ternaryOperator() {
    boolean a = true;
    String d = a ? "foo" : "bar";
    System.out.println(d);
  }
}
