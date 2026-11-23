public class InheritedField {

    public static void main(String[] args) {
        Sub s = new Sub(); // @1
        Value v = new Value(); // @2
        s.f = v; // field reference <InheritedField$Sub: f>
        Value r = read(s); // loads <InheritedField$Super: f>
        Sub.sf = new Value(); // @3, static field reference <InheritedField$Sub: sf>
        Value q = Super.sf; // static field reference <InheritedField$Super: sf>
    }

    static Value read(Super o) {
        return o.f;
    }

    static class Super {
        Value f;
        static Value sf;
    }

    static class Sub extends Super {
    }

    static class Value {
    }

}
