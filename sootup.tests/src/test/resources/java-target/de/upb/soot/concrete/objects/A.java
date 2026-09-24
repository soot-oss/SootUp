package de.upb.sootup.concrete.objects;

/**
 * @author Manuel Benz created on 12.07.18
 */
public class A {
  public void voidM() {
    System.out.println("foo");
  }

  public String returnM() {
    return "bar";
  }

  public void argsM(String foo, String bar) {
    System.out.println(foo);
    System.out.println(bar);
  }

  public void argsVar(String... foo) {
    for (String s : foo) {
      System.out.println(s);
    }
  }

  @Override
  public String toString() {
    return "A";
  }
}
