// j8dim/Class.java
package j8dim0;

// import lib.annotations.callgraph.DirectCall;

class Class implements Interface {

  public void method(){

  }
  public static void main(String[] args){
    Interface i = new Class();
    i.method();
  }
}

interface Interface {
  public void method();
}