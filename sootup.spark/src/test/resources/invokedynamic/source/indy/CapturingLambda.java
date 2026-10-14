package indy;

class CapturingLambda {

  public static void main(String[] args) {
    Payload p = new Payload();
    Runnable r = () -> sink(p);
    r.run();
  }

  static void sink(Object x) {}
}
