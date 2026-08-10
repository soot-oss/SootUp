public class ReflectiveArityBound {

  static class TooManyCtorArgs {
    TooManyCtorArgs(int a, int b) {
      ctorTouch();
    }

    static void ctorTouch() {}
  }

  static class OneCtorArg {
    OneCtorArg(int a) {
      oneArgCtorTouch();
    }

    static void oneArgCtorTouch() {}
  }

  static class TooManyMethodParams {
    void bigMethod(int a, int b, int c) {
      bigMethodTouch();
    }

    static void bigMethodTouch() {}
  }

  public static void main(String[] args) throws Exception {
    Class<?> c1 = Class.forName("ReflectiveArityBound$TooManyCtorArgs");
    Object viaTooManyCtorArgs = c1.getConstructor().newInstance();

    Class<?> c2 = Class.forName("ReflectiveArityBound$OneCtorArg");
    Object viaOneCtorArg = c2.getConstructor().newInstance();

    Class<?> c3 = Class.forName("ReflectiveArityBound$TooManyMethodParams");
    Object viaCtor = c3.getConstructor().newInstance();
    java.lang.reflect.Method m = c3.getMethod("bigMethod");
    m.invoke(viaCtor);
  }
}
