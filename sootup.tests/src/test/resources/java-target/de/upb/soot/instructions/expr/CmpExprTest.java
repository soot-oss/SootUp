package de.upb.sootup.instructions.expr;

public class CmpExprTest {

  public void cmpOperatorNotEqual(long a, long b) {
    boolean result = a != b;
    System.out.println(result);

  }

  public void cmpOperatorEqual(long a, long b) {
    boolean result = a == b;
    System.out.println(result);

  }

  public void cmpOperatorLower(long a, long b) {
    boolean result = a < b;
    System.out.println(result);

  }

  public void cmpOperatorLowerEqual(long a, long b) {
    boolean result = a <= b;
    System.out.println(result);

  }

  public void cmpOperatorGreater(long a, long b) {
    boolean result = a > b;
    System.out.println(result);
  }

  public void cmpOperatorGreaterEqual(long a, long b) {
    boolean result = a >= b;
    System.out.println(result);
  }

}