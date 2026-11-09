package de.upb.sootup.concrete.emulation;

/**
 * @author Manuel Benz created on 11.07.18
 */
public class Objects {

  public void jObjectToJavaObject() {
    System.out.println(new Foo());
  }

  public void systemOut() {
    System.out.println();
  }

  private static final class Foo {

    public Foo() {
    }

    @Override
    public String toString() {
      return "foo";
    }
  }
}
