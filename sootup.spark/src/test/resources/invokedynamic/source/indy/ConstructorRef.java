package indy;

import java.util.function.Supplier;

class ConstructorRef {

  public static void main(String[] args) {
    Supplier<Worker> s = Worker::new;
    s.get();
  }
}
