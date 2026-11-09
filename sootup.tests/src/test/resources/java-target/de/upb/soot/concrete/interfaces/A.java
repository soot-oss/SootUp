package de.upb.sootup.concrete.interfaces;

/**
 * @author Manuel Benz created on 12.07.18
 */
public class A extends B implements I2 {
  @Override
  public void printI2() {
    System.out.println("implements i2");
  }
}
