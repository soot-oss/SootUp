package de.upb.sootup.basic.fieldReference;

public class JFieldReference {
  public static void JFieldReference(String[] args) {
    A a1 = new A();
    int a = a1.i;
    String c = a1.j;
    int b = 20;
    int max_int = java.lang.Integer.MAX_VALUE;
    float max_float = java.lang.Float.MAX_VALUE;
    System.out.println(b + " is " + c + " than " + a);
    System.out.println(max_int);
    System.out.println(max_float);
  }
}