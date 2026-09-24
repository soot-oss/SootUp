package de.upb.sootup.instructions.expr;

public class SpecialInvokeExprTest {

  private double someMethod() {
    return 3.14;
  }

  void local() {

    SpecialInvokeExprTest inv = new SpecialInvokeExprTest();
    double d = inv.someMethod();
    System.out.println(d);

  }

  void method() {

    double e = someMethod();
    System.out.println(e);

  }

  void fromsuperclass() {

    Object fromsuperclass = (Object) this;
    int f = fromsuperclass.hashCode();
    System.out.println(f);

  }

}
