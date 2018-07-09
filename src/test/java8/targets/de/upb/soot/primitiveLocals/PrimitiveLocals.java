package de.upb.soot.primitiveLocals;

public class PrimitiveLocals {
  public void primitive_int() {
    int a = 512;
    System.out.println(a);
    a++;
  }

  public void primitive_byte() {
    byte b = 0;
    System.out.println(b);
    b++;
  }

  public void primitive_char() {
    char c = 'a';
    System.out.println(c);
    c = 'b';
    System.out.println(c);
  }

  public void primitive_short() {
    short d = 10;
    System.out.println(d);
    d++;
  }

  public void primitive_float() {
    float e = 3.14f;
    System.out.println(e);
    e = 7.77f;
    System.out.println(e);
  }

  public void primitive_long() {
    long f = 123456789;
    System.out.println(f);
    f = 123456777;
    System.out.println(f);

  }

  public void primitive_double() {
    double g = 1.96969654d;
    System.out.println(g);
    g = 1.787777777;
    System.out.println(g);
  }
}
