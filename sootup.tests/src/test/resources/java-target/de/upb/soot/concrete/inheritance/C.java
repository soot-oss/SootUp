package de.upb.sootup.concrete.inheritance;

/**
 * @author Manuel Benz created on 12.07.18
 */
public class C extends B {

  public int a = 4;

  @Override
  public void methodA() {
    System.out.println("methodAinC");
  }

  public void methodC() {
    System.out.println("methodC");
  }
}
