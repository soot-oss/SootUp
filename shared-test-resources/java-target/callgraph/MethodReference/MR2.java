// id/Class.java
// package id;

// import lib.annotations.callgraph.IndirectCall;

class Class {

  private String getTypeName() { return "Lid/Class;";}

  /*@IndirectCall(
          name = "getTypeName", returnType = String.class, line = 14,
          resolvedTargets = "Lid/Class;") */
  public void callViaMethodReference(){
    java.util.function.Supplier<String> stringSupplier = this::getTypeName;
    stringSupplier.get();
  }

  public static void main(String[] args){
    Class cls = new Class();
    cls.callViaMethodReference();
  }
}
