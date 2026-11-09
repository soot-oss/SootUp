package de.upb.sootup.instructions.stmt;

interface IThing {
  void printId();
}

class Pear implements IThing {

  @Override
  public void printId() {
    System.out.println("Id");
  }

}

public class InvokeStmtTest {

  private void someMethod() {
    System.out.println("Sth");
  }

  public static void somethingStatic() {
    System.out.println("Polyester Shirt");
  }

  public void specialInvoke() {

    InvokeStmtTest inv = new InvokeStmtTest();
    inv.someMethod();

    someMethod();

  }

  public void interfaceInvoke() {

    IThing temp = new Pear();
    temp.printId();

  }

  public void virtualInvoke() {

    System.out.println("virtual invoked");

  }

  public void staticInvoke() {

    somethingStatic();

    System.gc();

  }

}
