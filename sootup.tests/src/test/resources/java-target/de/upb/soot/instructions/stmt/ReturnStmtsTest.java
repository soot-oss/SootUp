package de.upb.sootup.instructions.stmt;

public class ReturnStmtsTest {

  ReturnStmtsTest(int a) {
    if (a < 18) {
      return;
    }
  }

  // JReturnVoid
  public void returnnothing() {
    System.out.println("A");
    return;
  }

  // JReturnVoid
  public void pointofnoreturn() {
    System.out.println("A");
  }

  public int returnA(int a) {
    return a;
  }

  public int returnB() {
    int b = 42;
    return b;
  }

  public char returnC() {
    return 'c';
  }

  public Integer returnObject() {
    return new Integer(5);
  }

}
