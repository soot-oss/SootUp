package de.upb.sootup.instructions.expr;

public class CmplExprTest {

  public void cmplOperatorNotEqual(float a, float b) {
    boolean result = a != b;
    System.out.println(result);
  }

  public void cmplOperatorEqual(float a, float b) {
    boolean result = a == b;
    System.out.println(result);
  }

  public void cmplOperatorGreater(float a, float b) {
    boolean result = a > b;
    System.out.println(result);
  }

  public void cmplOperatorGreaterEqual(float a, float b) {
    boolean result = a >= b;
    System.out.println(result);
  }

}
