public class ReflectiveInstantiationIndirect {

  static class Widget {
    Widget() {
      touch();
    }

    static void touch() {}
  }

  static Object createViaLiteral(String className) throws Exception {
    return Class.forName(className).newInstance();
  }

  static Object createViaWrapper(String className) throws Exception {
    return createViaLiteral(className);
  }

  // A separate, unshared wrapper chain for the computed-name case: sharing createViaLiteral /
  // createViaWrapper above with a computed (non-literal) argument would make its parameter's
  // points-to set the union of the literal calls' argument AND the computed one -- correct,
  // sound behavior for a context-insensitive analysis, but it would make this fixture unable to
  // tell "does a literal argument resolve" apart from "does merging with an unrelated literal
  // call site resolve".
  static Object createViaLiteralUnrelated(String className) throws Exception {
    return Class.forName(className).newInstance();
  }

  static Object createViaWrapperUnrelated(String className) throws Exception {
    return createViaLiteralUnrelated(className);
  }

  public static void main(String[] args) throws Exception {
    Object viaOneHop = createViaLiteral("ReflectiveInstantiationIndirect$Widget");
    Object viaTwoHops = createViaWrapper("ReflectiveInstantiationIndirect$Widget");

    Object viaComputedName = createViaWrapperUnrelated(args[0]);
  }
}
