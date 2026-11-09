package de.upb.sootup.concrete.inheritance;

public class Inheritance {
  public void dynDispatch1() {
    A b = new D();
    b.print();
    b.methodA();
  }

  public void dynDispatch2() {
    A b = new B();
    b.print();
    b.methodA();
  }

  public void singleLevel() {
    B b = new B();
    b.methodB();
    b.print();
    b.methodA();
  }

  public void twoLevels() {
    C b = new C();
    b.methodB();
    b.print();
    b.methodA();
    b.methodC();
  }

  public void fieldOverwrite() {
    final C c = new C();
    System.out.println(c.a);
    System.out.println(c.b);
    System.out.println(((B) c).a);
  }

  public void constructorOverwrite() {
    SubConstructor sub = new SubConstructor();
  }

  public void constructorOverwriteArg() {
    SubConstructor sub = new SubConstructor("sup");
  }

  public void nestedClass() {
    final X x = new X();
    x.print();
    x.methodA();
  }

  public void staticNestedClass() {
    Y y = new Y();
    y.print();
    y.methodA();
  }

  public void anonymousInheritance() {
    A a = new A() {
      @Override
      void print() {
        System.out.println("Abstract");
      }
    };
    a.print();
    a.methodA();
  }

  private static class Y extends A {

    @Override
    void print() {
      System.out.println("Y");
    }
  }

  private class X extends A {

    @Override
    void print() {
      System.out.println("X");
    }
  }
}
