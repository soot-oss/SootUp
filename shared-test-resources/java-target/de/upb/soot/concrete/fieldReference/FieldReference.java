package de.upb.sootup.concrete.fieldReference;

public class JFieldReference {
  private final int f = 2;
  protected String b = "foo";
  private int a = 1;

  public void ownField() {
    System.out.println(a);
  }

  public void ownFieldWrite() {
    a++;
    System.out.println(a);
  }

  public void otherField() {
    A other = new A();
    System.out.println(other.i);
  }

  public void otherFieldWrite() {
    A other = new A();
    other.i++;
    System.out.println(other.i);
  }

  public void otherFieldWrite2() {
    A other = new A();
    int aO = other.i++;
    System.out.println(aO);
  }

  public void ownFieldObject() {
    System.out.println(b);
  }

  public void ownFieldWriteObject() {
    b = "new";
    System.out.println(b);
  }

  public void otherFieldObject() {
    A other = new A();
    System.out.println(other.j);
  }

  public void otherFieldWriteObject() {
    A other = new A();
    other.j = "new";
    System.out.println(other.j);
  }

  public void otherFieldWrite2Object() {
    A other = new A();
    String old = other.j;
    other.j = "new";
    System.out.println(old);
  }

  public void finalField() {
    System.out.println(b);
  }

}