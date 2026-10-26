package indy;

import java.util.function.Function;

class UnboundRef {

  public static void main(String[] args) {
    Function<Payload, Payload> g = Payload::self;
    Payload r = g.apply(new Payload());
    sink(r);
  }

  static void sink(Object o) {}
}
