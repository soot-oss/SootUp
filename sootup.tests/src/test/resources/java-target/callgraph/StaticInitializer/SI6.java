// si/Main.java
// package si;

// import lib.annotations.callgraph.DirectCall;
public class Main {

  public static void main(String[] args) {
    Demo.callback();
  }
}

class Demo {
  static String name = init();

//  @DirectCall(name = "callback", line = 16, resolvedTargets = "Lsi/Demo;")
  static String init() {
    callback();
    return "42";
  }

  static void callback() {}
}