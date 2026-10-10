package indy;

import java.util.function.Supplier;

class CtorRefResult {

  public static void main(String[] args) {
    Supplier<Worker> s = Worker::new;
    Worker w = s.get();
    sink(w);
  }

  static void sink(Object o) {}
}
