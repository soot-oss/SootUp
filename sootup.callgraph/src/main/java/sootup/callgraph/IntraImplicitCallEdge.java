package sootup.callgraph;

public class IntraImplicitCallEdge extends ImplicitCallEdge {
  Boolean resolveCalleeClassName;
  String superClass;

  protected IntraImplicitCallEdge(
      String id,
      int category,
      MethodSpec caller,
      MethodSpec callee,
      Boolean resolveCalleeClassName,
      String superClass) {
    super(id, category, caller, callee);
    this.resolveCalleeClassName = resolveCalleeClassName;
    this.superClass = superClass;
  }

  public Boolean getResolveCalleeClassName() {
    return resolveCalleeClassName;
  }

  public String getSuperClass() {
    return superClass;
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
        + ", superClass='"
        + superClass
        + '\''
        + '}';
  }
}
