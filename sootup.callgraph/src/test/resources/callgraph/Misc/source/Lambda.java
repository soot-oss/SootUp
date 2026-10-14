package lambda;

import java.util.function.Function;

class Class {

  public static void main(String[] args) {
    Function<String, String> f = s -> target(s);
    f.apply("x");
  }

  static String target(String s) {
    return s;
  }
}
