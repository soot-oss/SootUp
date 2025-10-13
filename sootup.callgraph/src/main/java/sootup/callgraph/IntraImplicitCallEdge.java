package sootup.callgraph;

public class IntraImplicitCallEdge extends ImplicitCallEdge {
  Boolean resolveCalleeClassName;

  protected IntraImplicitCallEdge(
      String id, int category, MethodSpec caller, MethodSpec callee, Boolean resolveCalleeClassName) {
    super(id, category, caller, callee);
    this.resolveCalleeClassName = resolveCalleeClassName;
  }

  public Boolean getResolveCalleeClassName() {
    return resolveCalleeClassName;
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
        + '}';
  }
}
