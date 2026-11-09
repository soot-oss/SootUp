package chacache;

class Example {

  public static void main(String[] args) {
    A obj = new B();
    obj.virtualDispatch();
  }
}

class A {
  public void virtualDispatch() { }
}

class B extends A {
  public void virtualDispatch() { }
}
