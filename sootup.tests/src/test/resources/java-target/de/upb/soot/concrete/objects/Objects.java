package de.upb.sootup.concrete.objects;

/**
 * @author Manuel Benz created on 12.07.18
 */
public class Objects {

  public void emptyConstructor() {
    A a = new A();
    System.out.println("A");
  }

  public void singleConstructor() {
    B a = new B();
    System.out.println("B");
  }

  public void methodCall() {
    new A().voidM();
  }

  public void methodCallReturn() {
    System.out.println(new A().returnM());
  }

  public void methodCallArgs() {
    new A().argsM("foo", "bar");
  }

  public void methodCallVarArgs() {
    new A().argsVar("foo", "bar");
  }
}
