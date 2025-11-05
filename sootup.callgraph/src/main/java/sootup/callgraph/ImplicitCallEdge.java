package sootup.callgraph;

import com.google.gson.annotations.SerializedName;

/** The abstract class for all possible categories of implicit call edges. */
public abstract class ImplicitCallEdge {

  protected String id;
  protected int category;
  protected MethodSpec caller;
  protected MethodSpec callee;

  protected ImplicitCallEdge(String id, int category, MethodSpec caller, MethodSpec callee) {
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

  public MethodSpec getCaller() {
    return caller;
  }

  public MethodSpec getCallee() {
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

  /** Class to resolve the caller and callee information from <code>ImplicitPatterns.json</code>. */
  public static class MethodSpec {
    @SerializedName(
        value = "fullyQualifiedClassName",
        alternate = {"callerFullyQualifiedClassName", "calleeFullyQualifiedClassName"})
    protected String fullyQualifiedClassName;

    @SerializedName(
        value = "name",
        alternate = {"callerName", "calleeName"})
    protected String name;

    @SerializedName(
        value = "param",
        alternate = {"callerParam", "calleeParam"})
    private String param;

    @SerializedName(
        value = "returnType",
        alternate = {"callerReturnType", "calleeReturnType"})
    private String returnType;

    public MethodSpec(
        String fullyQualifiedClassName, String name, String param, String returnType) {
      this.fullyQualifiedClassName = fullyQualifiedClassName;
      this.name = name;
      this.param = param;
      this.returnType = returnType;
    }

    public String getFullyQualifiedClassName() {
      return fullyQualifiedClassName;
    }

    public String getName() {
      return name;
    }

    public String getParam() {
      return param;
    }

    public String getReturnType() {
      return returnType;
    }

    public void setFullyQualifiedClassName(String fullyQualifiedClassName) {
      this.fullyQualifiedClassName = fullyQualifiedClassName;
    }

    public void setName(String name) {
      this.name = name;
    }

    public void setParam(String param) {
      this.param = param;
    }

    public void setReturnType(String returnType) {
      this.returnType = returnType;
    }

    @Override
    public String toString() {
      return "MethodSpec{"
          + "fullyQualifiedClassName='"
          + fullyQualifiedClassName
          + '\''
          + ", name='"
          + name
          + '\''
          + ", param='"
          + param
          + '\''
          + ", returnType='"
          + returnType
          + '\''
          + '}';
    }
  }
}
