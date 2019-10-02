// si/Main.java
// package si;

// import lib.annotations.callgraph.DirectCall;
public class Main {

  public static void main(String[] args) {
    new Subclass();
  }
}

class Subclass extends Superclass {
  static String name = init();

//  @DirectCall(name = "callback", line = 16, resolvedTargets = "Lsi/Subclass;")
  static String init() {
    callback();
    return "Subclass";
  }

  static void callback() {}
}

class Superclass extends RootClass {

  static {
    superInit();
  }

//  @DirectCall(name = "callback", line = 31, resolvedTargets = "Lsi/Superclass;")
  static void superInit(){
    callback();
  }

  static void callback() {}
}

class RootClass {

  static {
    rootInit();
  }

//  @DirectCall(name = "callback", line = 45, resolvedTargets = "Lsi/RootClass;")
  static void rootInit(){
    callback();
  }

  static void callback() {}
}