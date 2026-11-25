public class InheritedMethod {

    public static void main(String[] args) {
        Value v = new Value(); // @1
        Value r = Sub.id(v); // static invoke <InheritedMethod$Sub: id>, declared in Super
        Sub s = new Sub(); // @2
        Value w = s.viaSuper(v); // special invoke <InheritedMethod$Mid: get> in viaSuper, declared in Super
    }

    static class Super {
        static Value id(Value x) {
            return x;
        }

        Value get(Value x) {
            return x;
        }
    }

    static class Mid extends Super {
    }

    static class Sub extends Mid {
        Value viaSuper(Value x) {
            return super.get(x);
        }
    }

    static class Value {
    }

}
