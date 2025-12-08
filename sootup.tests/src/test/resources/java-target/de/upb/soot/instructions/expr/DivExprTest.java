package de.upb.sootup.instructions.expr;

public class DivExprTest {

  public void division(int a, int b) {

    int romans = b / a;
    System.out.println(romans);

  }

  public void divisionAssignment(int a, int jcaesar) {

    jcaesar /= a;
    System.out.println(jcaesar);

  }

}
