package example1;

class Example {

  public static void main1(String[] args) {
    A objB = new B();
    A objC = new C();

    objB.print(objC);
  }
}

class A extends Object {
  public void print( Object o) { }
}

class B extends A {
  public void print(Object o) { }
}

class C extends B {
  public void print(Object o) { }
}

class D extends A {
  public void print(Object o) { }
}
