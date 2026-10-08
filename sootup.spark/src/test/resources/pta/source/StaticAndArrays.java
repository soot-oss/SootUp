public class StaticAndArrays {

  interface Shape {
    String name();
  }

  static class Circle implements Shape {
    public String name() {
      return "circle";
    }
  }

  static class Square implements Shape {
    public String name() {
      return "square";
    }
  }

  static Shape global;

  public static void main(String[] args) {
    store(new Circle());
    Shape fromStatic = load();
    fromStatic.name();

    Shape[] shapes = new Shape[2];
    shapes[0] = new Square();
    int i = args.length;
    Shape fromArray = shapes[i];
    fromArray.name();
  }

  static void store(Shape s) {
    global = s;
  }

  static Shape load() {
    return global;
  }
}
