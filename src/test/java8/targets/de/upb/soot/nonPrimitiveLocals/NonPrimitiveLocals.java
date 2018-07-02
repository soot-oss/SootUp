package de.upb.soot.nonPrimitiveLocals;

public class NonPrimitiveLocals {
  public void non_primitive_locals(String[] args) {
    int a[] = new int[10];
    A a1 = new A();
    a1.foo();
    a[1] = 10;
    a[7] = 70;
  }
}
