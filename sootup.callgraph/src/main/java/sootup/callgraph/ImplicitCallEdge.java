package sootup.callgraph;

/** The class for all possible categories of implicit call edges. */
public class ImplicitCallEdge {

  protected final String id;
  protected final int category;
  protected final Caller caller;
  protected final Callee callee;

  protected ImplicitCallEdge(String id, int category, Caller caller, Callee callee) {
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

  public Caller getCaller() {
    return caller;
  }

  public Callee getCallee() {
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

  public static class Caller {
    String callerFullyQualifiedClassName;
    String callerName;
    String callerParam;
    String callerReturnType;

    Caller(
        String callerFullyQualifiedClassName,
        String callerName,
        String callerParam,
        String callerReturnType) {
      this.callerFullyQualifiedClassName = callerFullyQualifiedClassName;
      this.callerName = callerName;
      this.callerParam = callerParam;
      this.callerReturnType = callerReturnType;
    }

    public String getCallerFullyQualifiedClassName() {
      return callerFullyQualifiedClassName;
    }

    public String getCallerName() {
      return callerName;
    }

    public String getCallerParam() {
      return callerParam;
    }

    public String getCallerReturnType() {
      return callerReturnType;
    }
  }

  public static class Callee {
    String calleeFullyQualifiedClassName;
    String calleeName;
    String calleeParam;
    String calleeReturnType;

    Callee(
        String calleeFullyQualifiedClassName,
        String calleeName,
        String calleeParam,
        String calleeReturnType) {
      this.calleeFullyQualifiedClassName = calleeFullyQualifiedClassName;
      this.calleeName = calleeName;
      this.calleeParam = calleeParam;
      this.calleeReturnType = calleeReturnType;
    }

    public String getCalleeFullyQualifiedClassName() {
      return calleeFullyQualifiedClassName;
    }

    public String getCalleeName() {
      return calleeName;
    }

    public String getCalleeParam() {
      return calleeParam;
    }

    public String getCalleeReturnType() {
      return calleeReturnType;
    }
  }
}
