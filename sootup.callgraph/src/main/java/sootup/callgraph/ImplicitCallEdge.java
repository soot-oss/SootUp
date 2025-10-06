package sootup.callgraph;

/** The abstract class for all possible categories of implicit call edges. */
public abstract class ImplicitCallEdge {

  protected final String id;
  protected final int category;
  protected final FixCaller caller;
  protected final FixCallee callee;

  protected ImplicitCallEdge(String id, int category, FixCaller caller, FixCallee callee) {
    this.id = id;
    this.category = category;
    this.caller = caller;
    this.callee = callee;
  }

  public String getId() {
    return id;
  }

  public int getCategory() {
    return category;
  }

  public FixCaller getCaller() {
    return caller;
  }

  public FixCallee getCallee() {
    return callee;
  }

  @Override
  public String toString() {
    return "ImplicitCallEdge{"
        + "id='"
        + id
        + '\''
        + ", category="
        + category
        + ", caller="
        + caller
        + ", callee="
        + callee
        + '}';
  }

  public static class FixCaller {
    String callerSig;

    FixCaller(String callerSig) {
      this.callerSig = callerSig;
    }
  }

  public static class FixCallee {
    String calleeSig;

    FixCallee(String calleeSig) {
      this.calleeSig = calleeSig;
    }
  }
}
