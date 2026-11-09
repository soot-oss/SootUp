package de.upb.sootup.instructions.expr;

public class RemExprTest {

  public void modulus(int a, int b) {
    int d = b % a;
    System.out.println(d);
  }

  public void modulusAssign(int a, int b) {
    b %= a;
    System.out.println(b);
  }

}
