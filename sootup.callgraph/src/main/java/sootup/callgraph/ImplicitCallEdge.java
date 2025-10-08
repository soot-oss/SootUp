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
    String callerClassName;
    String callerPackage;
    String callerName;
    String callerParam;
    String callerReturnType;

    Caller(
        String callerClassName,
        String callerPackage,
        String callerName,
        String callerParam,
        String callerReturnType) {
      this.callerClassName = callerClassName;
      this.callerPackage = callerPackage;
      this.callerName = callerName;
      this.callerParam = callerParam;
      this.callerReturnType = callerReturnType;
    }

    public String getCallerClassName() {
      return callerClassName;
    }

    public String getCallerPackage() {
      return callerPackage;
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
    String calleeClassName;
    String calleePackage;
    String calleeName;
    String calleeParam;
    String calleeReturnType;

    Callee(
        String calleeClassName,
        String calleePackage,
        String calleeName,
        String calleeParam,
        String calleeReturnType) {
      this.calleeClassName = calleeClassName;
      this.calleePackage = calleePackage;
      this.calleeName = calleeName;
      this.calleeParam = calleeParam;
      this.calleeReturnType = calleeReturnType;
    }

    public String getCalleeClassName() {
      return calleeClassName;
    }

    public String getCalleePackage() {
      return calleePackage;
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
