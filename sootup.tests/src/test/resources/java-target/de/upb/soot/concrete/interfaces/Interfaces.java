package de.upb.sootup.concrete.interfaces;

public class Interfaces {
  public void singleInterface() {
    B i1 = new B();
    i1.printI1();
  }

  public void singleInterfaceDynDispatch() {
    I1 i1 = new B();
    i1.printI1();
  }

  public void multipleInterface() {
    C c = new C();
    c.printI1();
    c.printI2();
  }

  public void inheritanceAndInterface() {
    final A a = new A();
    a.printI2();
    a.printI1();
  }

  public void overwriteInterfaceMethod() {
    final D d = new D();
    d.printI2();
    d.printI1();
    ((C) d).printI2();
  }

  public void anonymousImpl() {
    I2 i2 = new I2() {
      @Override
      public void printI2() {
        System.out.println("anonymous i2");
      }
    };
    i2.printI2();
  }
}
