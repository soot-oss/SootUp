public class FieldAssignment {
    private static class A {
        String s;
    }

    public static void entry() {
        A a = new A();
        String b = "abc";

        a.s = b;
    }
}
