package polarity;

class Target {
  public static void method() {}
}

class Trigger extends Thread {
  @Override
  public void run() {}
}

class WithClinit {
  static int x = compute();

  static int compute() {
    return 1;
  }
}

class Main {
  public static void main(String[] args) {
    excludedCaller();
  }

  public static void excludedCaller() {
    Target.method();
    new Trigger().start();
    int y = WithClinit.x;
  }
}
