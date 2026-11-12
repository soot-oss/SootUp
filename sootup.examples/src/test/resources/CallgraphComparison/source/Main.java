public class Main {

  static Shape id(Shape s) {
    return s;
  }

  public static void main(String[] args) {
    Shape circle = new Circle();
    circle.area(); // call site 1

    Shape triangle = id(new Triangle()); // the result is never used
    Shape square = id(new Square());
    square.area(); // call site 2
  }
}
