package de.upb.soot.Interfaces;

public class Interfaces {
  public void single_interface() {
    B b1 = new B();
    b1.printA1();
  }
  public void multiple_interface() {
    C c1 = new C();
    c1.printA1();
    c1.printA2();
  }
}
