package multi;

class MultipleCallsToSameTarget {

    public static void staticMethod(){}

    public void method(){}

    public static void main(String[] args){
      MultipleCallsToSameTarget.staticMethod();
      MultipleCallsToSameTarget.staticMethod();

      MultipleCallsToSameTarget in1 = new MultipleCallsToSameTarget();
      MultipleCallsToSameTarget in2 = new MultipleCallsToSameTarget();

      in1.method();
      in2.method();
    }
}