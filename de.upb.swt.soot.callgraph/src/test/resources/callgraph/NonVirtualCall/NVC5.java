// nvc/Demo.java
package nvc5;

// import lib.annotations.callgraph.DirectCall;

public class Demo {

  public static void main(String[] args){
    new Sub().method();
  }
}

class Super {

  void method() { /* doSomething */ }
}

class Middle extends Super {

  void method() { /* doSomething */ }
}

class Sub extends Middle {

 // @DirectCall(name="method", line=26, resolvedTargets = "Lnvc/Middle;")
  void method() {
    super.method();
  }
}