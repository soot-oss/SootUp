package tfx;

/** Reflectively created receiver used by a second reflective call in the same method. */
public class ChainedReflection {
  public static void main(String[] args) throws Exception {
    Object w = Class.forName("tfx.RealWorker").getConstructor().newInstance();
    Object r = Worker.class.getMethod("work", Object.class).invoke(w, "x");
  }
}
