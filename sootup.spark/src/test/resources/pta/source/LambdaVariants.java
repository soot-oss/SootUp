import java.util.function.Supplier;

public class LambdaVariants {

    static class Value {
    }

    static class Box {
        Value value;

        Value getValue() {
            return value;
        }
    }

    static Value identity(Value v) {
        return v;
    }

    public static void main(String[] args) {
        Value seed = new Value();
        Supplier<Value> capturing = () -> identity(seed); // REF_INVOKE_STATIC, 1 capture
        Value viaCapture = capturing.get();

        Box box = new Box();
        Supplier<Value> boundRef = box::getValue; // REF_INVOKE_VIRTUAL, capture 0 = receiver

        Supplier<Value> ctorRef = Value::new; // REF_INVOKE_CONSTRUCTOR, no captures
        Value viaCtor = ctorRef.get();
    }
}
