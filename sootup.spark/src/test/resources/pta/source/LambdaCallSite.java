import java.util.function.Supplier;

public class LambdaCallSite {

    public static void main(String[] args) {
        Container c = new Container();
        Value v1 = new Value();
        Value v2 = c.getValue(v1); // instance method call, alongside the invokedynamic below
        Supplier<Value> supplier = () -> new Value(); // invokedynamic call site (lambda)
        Value v3 = supplier.get();
    }

    static class Container {
        Value value;

        public Value getValue(Value input) {
            this.value = input;
            return input;
        }
    }

    static class Value {
    }
}
