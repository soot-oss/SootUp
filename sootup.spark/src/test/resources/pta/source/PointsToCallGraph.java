public class PointsToCallGraph {

    public static void main(String[] args) {
        Shape s = new Circle();
        Value a = s.area(); // CHA: Circle.area and Square.area; points-to: Circle.area only
        Value b = Util.id(a); // static call, kept
    }

    interface Shape {
        Value area();
    }

    static class Circle implements Shape {
        public Value area() {
            return new Value();
        }
    }

    // never instantiated: Square.area and what only it calls are not reachable
    static class Square implements Shape {
        public Value area() {
            return OnlyFromSquare.make();
        }
    }

    static class OnlyFromSquare {
        static Value make() {
            return new Value();
        }
    }

    static class Util {
        static Value id(Value v) {
            return v;
        }
    }

    static class Value {
    }

}
