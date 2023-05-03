package example1;

class Example {

  public static void main(String[] args) {
    A objB = new B();
    B.staticDispatch(new C());

    A objC=new E();

    objB.virtualDispatch();
  }
}

class A extends Object {
  public void virtualDispatch() { }
  public static void staticDispatch( Object o) { }
}

class B extends A {
  public void virtualDispatch() { }
  public static void staticDispatch( Object o) { }
}

class C extends D {
  public static void staticDispatch( Object o) { }
}

class D extends A {
  public void virtualDispatch() { }
  public static void staticDispatch( Object o) { }
}

class E extends A {
  public void virtualDispatch() { }
  public static void staticDispatch( Object o) { }
}
