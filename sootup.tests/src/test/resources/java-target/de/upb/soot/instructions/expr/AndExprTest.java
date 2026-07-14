package de.upb.sootup.instructions.expr;

public class AndExprTest {

  public void logicalAND(boolean a, boolean b) {
    boolean result = (a && b);
    System.out.println(result);
  }

  public void bitwiseAND(int a, int b) {
    int result = (a & b);
    System.out.println(result);
  }

  public void bitwiseANDAssignment(int a) {
    int d = 0;
    d &= 2;
    System.out.println(d);
  }

}
