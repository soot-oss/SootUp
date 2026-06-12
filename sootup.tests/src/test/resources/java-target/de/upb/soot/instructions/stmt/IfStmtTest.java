package de.upb.sootup.instructions.stmt;

public class IfStmtTest {

  public void ifcondition(int a) {

    if (a < 42) {
      System.out.println("A");
    }

  }

  public void ifelsecondition(int a) {

    if (a < 42) {
      System.out.println("A");
    } else {
      System.out.println("B");
    }

  }

  public void ifelseifcondition(int a) {

    if (a < 42) {
      System.out.println("A");
    } else if (a > 42) {
      System.out.println("B");
    } else {
      System.out.println("C");
    }

  }

  public void ternary(int a, int b) {

    int c = a < 5 ? a : b;
    System.out.println(c);

  }

}
