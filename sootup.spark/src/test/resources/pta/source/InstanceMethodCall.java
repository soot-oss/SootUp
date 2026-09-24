public class InstanceMethodCall {

    public static void main(String[] args) {
        Container c = new Container();
        Value v1 = new Value();
        Value v2 = c.getValue(v1);  // instance method call
        Value v3 = c.processValue(v2);  // another instance method call
    }

    static class Container {
        Value value;
        
        public Value getValue(Value input) {
            this.value = input;
            return input;
        }
        
        public Value processValue(Value input) {
            return input;
        }
    }

    static class Value {
    }
}