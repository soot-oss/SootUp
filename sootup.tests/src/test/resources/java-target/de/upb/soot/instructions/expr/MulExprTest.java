package de.upb.sootup.instructions.expr;

public class MulExprTest {

  public void multiplication(int a, int b) {
    int d = b * a;
    System.out.println(d);
  }

  public void multiplicationAssignment(int a, int b) {
    b *= a;
    System.out.println(b);
  }

}