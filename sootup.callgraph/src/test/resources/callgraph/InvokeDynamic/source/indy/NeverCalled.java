package indy;

class NeverCalled {

  public static void main(String[] args) {
    Runnable r = () -> target();
  }

  static void target() {}
}
