package de.upb.sootup.basic.interfaces;

public class C implements A1, A2 {

  // implementing multiple interfaces
  public void printA1() {
    System.out.println("Interface A1");
  }

  public void printA2() {
    System.out.println("Interface A2");
  }
}
