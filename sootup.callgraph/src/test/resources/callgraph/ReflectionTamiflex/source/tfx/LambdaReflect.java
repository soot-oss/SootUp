package tfx;

/** Reflective call on a lambda: runtime target lives in a hidden class. */
public class LambdaReflect {
  public static void main(String[] args) throws Exception {
    Runnable r = () -> {};
    r.getClass().getMethod("run").invoke(r);
  }
}
