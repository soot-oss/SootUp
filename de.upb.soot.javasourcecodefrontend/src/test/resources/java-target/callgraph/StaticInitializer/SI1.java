// si/NonConstantFieldRef.java
package si1;

//import lib.annotations.callgraph.DirectCall;
public interface NonConstantFieldRef {

  static String nonConstantField = init();

 // @DirectCall(name = "callback", line = 10, resolvedTargets = "Lsi/NonConstantFieldRef;")
  static String init() {
    callback();
    return "Demo";
  }

  static void callback() {}
}

class Main {
  public static void main(String[] args) {
    NonConstantFieldRef.nonConstantField.toString();
  }
}