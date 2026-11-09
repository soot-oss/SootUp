package de.upb.sootup.instructions.expr;

public class CastExprTest {

  void number() {

    int d = 3;
    double pi = (double) d;
    System.out.println(pi);

  }

  void numberExplicit() {

    int d = 3;
    double pi = (double) d;
    System.out.println(pi);

  }

  void numberExplicitNeeded() {

    double d = 3.14;
    int pi = (int) d;
    System.out.println(pi);

  }

  void nonPrimitive() {

    Boolean B = new Boolean(true);
    Object obj = B;
    System.out.println(obj);

  }

  void nonPrimitiveExplicitNeeded() {

    Boolean B = new Boolean(true);
    Object obj = (Object) B;
    Boolean C = (Boolean) obj;
    System.out.println(C);

  }

}