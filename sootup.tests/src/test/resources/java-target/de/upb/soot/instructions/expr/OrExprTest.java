package de.upb.sootup.instructions.expr;

public class OrExprTest {

  public void logicalOR(boolean a, boolean b) {
    boolean result = a || b;
    System.out.println(result);
  }

  public void bitwiseOR(int a, int b) {
    int c = a | b;
    System.out.println(c);
  }

  public void bitwiseORAssignment(int a, int b) {
    b |= a;
    System.out.println(b);
  }

}
