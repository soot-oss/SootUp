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

  static class Triangle implements Shape {
    public String name() {
      return "triangle";
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

    Shape[][] grid = new Shape[2][2];
    grid[0][1] = new Triangle();
    Shape fromGrid = grid[i][i];
    fromGrid.name();

    Shape[][][] cube = new Shape[2][2][2];
    cube[1][0][1] = new Triangle();
    Shape fromCube = cube[i][i][i];
    fromCube.name();
  }

  static void store(Shape s) {
    global = s;
  }

  static Shape load() {
    return global;
  }
}
