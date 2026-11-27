public class SimplifySccs {

    public static void main(String[] args) {
        Object a = new A();
        Object b = new B();
        // a, b and t assign to each other in a cycle
        for (int i = 0; i < args.length; i++) {
            Object t = a;
            a = b;
            b = t;
        }
        // c flows through a parameter and a return in a cycle
        Object c = new C();
        for (int i = 0; i < args.length; i++) {
            c = id(c);
        }
        Object d = a;
    }

    static Object id(Object o) {
        return o;
    }

    static class A {}

    static class B {}

    static class C {}

}
