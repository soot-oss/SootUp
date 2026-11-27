public class VtaPreset {

    public static void main(String[] args) {
        Box b1 = new Box();
        Box b2 = new Box();
        b1.shape = new Circle();
        b2.shape = new Square();
        Shape s = b1.shape;
        s.area(); // field-sensitive: Circle; field-based (VTA): Circle and Square; CHA: also Triangle
    }

    static class Box {
        Shape shape;
    }

    interface Shape {
        int area();
    }

    static class Circle implements Shape {
        public int area() {
            return 1;
        }
    }

    static class Square implements Shape {
        public int area() {
            return 2;
        }
    }

    // never instantiated
    static class Triangle implements Shape {
        public int area() {
            return 3;
        }
    }

}
