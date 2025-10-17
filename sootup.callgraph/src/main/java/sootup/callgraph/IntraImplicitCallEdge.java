package sootup.callgraph;

public class IntraImplicitCallEdge extends ImplicitCallEdge {
  Boolean resolveCalleeClassName;
  String interfaceType;

  protected IntraImplicitCallEdge(
      String id,
      int category,
      MethodSpec caller,
      MethodSpec callee,
      Boolean resolveCalleeClassName,
      String interfaceType) {
    super(id, category, caller, callee);
    this.resolveCalleeClassName = resolveCalleeClassName;
    this.interfaceType = interfaceType;
  }

  public Boolean getResolveCalleeClassName() {
    return resolveCalleeClassName;
  }

  public String getInterfaceType() {
    return interfaceType;
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
        + ", resolveCalleeClassName="
        + resolveCalleeClassName
        + ", interfaceType='"
        + interfaceType
        + '\''
        + '}';
  }
}
