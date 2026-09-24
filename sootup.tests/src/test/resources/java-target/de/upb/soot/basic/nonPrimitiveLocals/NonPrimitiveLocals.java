package de.upb.sootup.basic.nonPrimitiveLocals;

public class NonPrimitiveLocals extends A {
  public void nonPrimitiveLocalsTarget() {
    int a[] = new int[10];
    A a1 = new A();
    a1.foo();
    a[1] = 10;
    a[7] = 70;
  }
}
