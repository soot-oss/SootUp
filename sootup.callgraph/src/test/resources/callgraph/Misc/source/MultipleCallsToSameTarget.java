package multi;

class MultiCalls {

    public static void staticMethod(){}

    public void method(){}

    public static void main(String[] args){
      MultiCalls.staticMethod();
      MultiCalls.staticMethod();

      MultiCalls in1 = new MultiCalls();
      MultiCalls in2 = new MultiCalls();

      in1.method();
      in2.method();
    }
}