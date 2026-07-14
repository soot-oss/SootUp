package de.upb.sootup.concrete.operators;

/**
 * @author Manuel Benz created on 11.07.18
 */
public class BooleanOps {

  public void logicalOr() {
    boolean a = true;
    boolean b = true;
    boolean result = (a || b);
    System.out.println(result);
  }

  public void logicalAnd() {
    boolean a = true;
    boolean b = true;
    boolean result = (a && b);
    System.out.println(result);
  }

  public void logicalNot() {
    boolean a = false;
    boolean result = !(a);
    System.out.println(result);
  }
}
