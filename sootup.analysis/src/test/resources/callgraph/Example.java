package example1;

class Example {

  public static void main(String[] args) {
    A objB = new B();
    B.staticDispatch();

    objB.virtualDispatch();
  }
}

class A extends Object {
  public void virtualDispatch() { }
  public static void staticDispatch() { }
}

class B extends A {
  public void virtualDispatch() { }
  public static void staticDispatch() { }
}
