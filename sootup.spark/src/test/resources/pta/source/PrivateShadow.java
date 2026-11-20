public class PrivateShadow {

    public static void main(String[] args) {
        Greeter g = new Impl();
        Object o = g.greet();  // runs Greeter.greet: Base.greet is private and does not override it
    }

    interface Greeter {
        default Object greet() {
            return new Object();
        }
    }

    static class Base {
        private Object greet() {
            return null;
        }
    }

    static class Impl extends Base implements Greeter {
    }
}
