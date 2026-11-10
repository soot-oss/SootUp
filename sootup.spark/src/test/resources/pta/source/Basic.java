public class Basic {

    public static void main(String[] args) {
        Field f1 = new Field(); // @4
        Field f2 = f1; // @4
        Container c1 = new Container(); // @6
        c1.field = f2;  // @4
        Container c2 = c1; // @6

    }

    static class Container {
        Field field;

    }

    static class Field {

    }

}