package tfx;

public interface Greeter {
  default String greet() {
    return "hi";
  }
}
