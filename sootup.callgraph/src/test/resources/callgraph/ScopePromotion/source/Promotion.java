package promo;

class Target {
  public static void foo() {
    bar();
  }

  public static void bar() {}
}

class Main {
  public static void main(String[] args) {
    Target.foo();
    Target.foo();
  }
}
