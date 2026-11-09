package de.upb.sootup.instructions.expr;

public class AddExprTest {

  public void addition(int a, int b) {
    int d = a + b;
    System.out.println(d);
  }

  public void addAssignmentOperator(int a) {
    int d = 0;
    d += a;
    System.out.println(d);
  }

}
