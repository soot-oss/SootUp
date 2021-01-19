// j8dim/Class.java
package j8dim1;

// import lib.annotations.callgraph.DirectCall;

class Class implements Interface {

  // @DirectCall(name = "method", line = 10, resolvedTargets = "Lj8dim/Interface;")
  public static void main(String[] args){
    Interface i = new Class();
    i.method();
  }
}

interface Interface {
  default void method() {
    // do something
  }
}