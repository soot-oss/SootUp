public class IgnoreBaseObjects {

    public static void main(String[] args) {
        Value v1 = new Value();
        Value v2 = new Value();

        Container c1 = new Container();
        Container c2 = new Container();

        // Two distinct base objects of the same type accessing the same field.
        // ignoreBaseObjects=false: c1.value and c2.value are separate field ref nodes.
        // ignoreBaseObjects=true:  both collapse to a single node keyed by (Container, value).
        c1.value = v1;
        c2.value = v2;

        Value r1 = c1.value;
        Value r2 = c2.value;
    }

    static class Container {
        Value value;
    }

    static class Value {

    }

}
