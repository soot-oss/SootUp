package receiver;

/** A call to an Object method on a receiver whose static type is an interface. */
public class Main {

  interface Shape {
    double area();
  }

  static class Square implements Shape {
    public double area() {
      return 1;
    }

    public String toString() {
      return "square";
    }
  }

  static class Unrelated {
    public String toString() {
      return "unrelated";
    }
  }

  public static void main(String[] args) {
    Shape shape = new Square();
    // refers to java.lang.Object.toString() on a receiver of type Shape, as dx encodes
    // shape.toString()
    ((Object) shape).toString();
    new Unrelated();
  }
}
