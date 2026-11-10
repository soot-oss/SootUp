package indy;

class InstanceLambda {

  Payload f = new Payload();

  public static void main(String[] args) {
    new InstanceLambda().go();
  }

  void go() {
    Runnable r = () -> sink(this.f);
    r.run();
  }

  static void sink(Object x) {}
}
