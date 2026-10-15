package indy;

import java.util.function.Function;

class LambdaArgs {

  public static void main(String[] args) {
    Function<Payload, Payload> f = x -> id(x);
    Payload q = f.apply(new Payload());
    sink(q);
  }

  static Payload id(Payload x) {
    return x;
  }

  static void sink(Object o) {}
}
