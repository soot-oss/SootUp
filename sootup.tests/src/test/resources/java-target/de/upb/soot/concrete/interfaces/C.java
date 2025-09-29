package de.upb.sootup.concrete.interfaces;

public class C implements I1, I2 {

  // implementing multiple interfaces
  public void printI1() {
    System.out.println("Interface I1");
  }

  public void printI2() {
    System.out.println("Interface I2");
  }
}
