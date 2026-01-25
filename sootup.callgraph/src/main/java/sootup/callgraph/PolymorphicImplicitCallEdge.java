package sootup.callgraph;
/**
 * For implicit method calls that do require an analysis to resolve the receiver's object type hierarchy.
 * Especially, the <code>Callee's</code> signature of the declaring class could be overridden.
 */

public class PolymorphicImplicitCallEdge extends ImplicitCallEdge {
  Boolean resolveCalleeClassName;
  String interfaceType;

  protected PolymorphicImplicitCallEdge(
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
