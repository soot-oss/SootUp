package de.upb.sootup.basic.interfaces;

public class Interfaces {
  public void singleInterface() {
    A1 a1 = new C();
    a1.printA1();
  }

  public void multipleInterface() {
    A1 a1 = new C();
    A2 a2 = new C();
    a1.printA1();
    a2.printA2();
  }
}
