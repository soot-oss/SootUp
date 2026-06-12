package de.upb.sootup.instructions.stmt;

public class IdentityStmtTest {

  int declProperty;
  int initProperty = 42;

  public void atThis() {

    System.out.println(this.declProperty);
    System.out.println(this.initProperty);

    System.out.println(declProperty);
    System.out.println(initProperty);

  }

  public void atParameterPrimitive(int a, boolean b) {

    System.out.println(a);
    System.out.println(b);

  }

  public void atParameterNonPrimitive(Integer i, String str, Boolean b, int[] arr) {

    System.out.println(i);
    System.out.println(str);
    System.out.println(b);
    System.out.println(arr);

  }

  // @caughtexception
  public void atExceptionThrow() throws Exception {

    throw new Exception("Issue");

  }

  public void atExceptionThrowAndCatch() {

    try {
      System.out.println("A1");
      int z = declProperty * initProperty;
      System.out.println(z);

    } catch (Exception e) {
      System.out.println("B1");
    }
    System.out.println("C1");

  }

  public void exceptionMultiple() {

    try {
      System.out.println("A2");
    } catch (IndexOutOfBoundsException e) {
      System.out.println("B2");
    } catch (Exception e) {
      System.out.println("C2");
    }
    System.out.println("D2");

  }

  public void exceptionFinally() {

    try {
      System.out.println("A3");
    } catch (Exception e) {
      System.out.println("B3");
    } finally {
      System.out.println("C3");
    }
    System.out.println("D3");

  }

}
