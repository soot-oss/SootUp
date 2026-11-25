package fastcha;

/** Virtual calls whose receiver local has a narrower type than the class the call refers to. */
public class Main {

  public static void main(String[] args) {
    A b = new B();
    // refers to A.virtualDispatch(), the local is typed B
    b.virtualDispatch();
    A g = new G();
    // G inherits A's implementation
    g.virtualDispatch();
    I k = new K();
    // K inherits I's default method
    k.defaultMethod();
    new E();
  }
}

class A {
  public void virtualDispatch() {}
}

class B extends A {
  public void virtualDispatch() {}
}

/** Never instantiated: a target nevertheless, as its type is a subtype of the receiver's */
class F extends B {
  public void virtualDispatch() {}
}

class E extends A {
  public void virtualDispatch() {}
}

class G extends A {}

interface I {
  default void defaultMethod() {}
}

class K implements I {}

class L implements I {
  public void defaultMethod() {}
}
