package sootup.callgraph;

/**
 * For implicit method calls that do not require an analysis to resolve the receiver's object type hierarchy.
 * The <code>Callee</code> method signature is "mostly" hardcoded in the <code>ImplicitPattern</code> file.
 */
public class StaticImplicitCallEdge extends ImplicitCallEdge {
  protected StaticImplicitCallEdge(String id, int category, MethodSpec caller, MethodSpec callee) {
    super(id, category, caller, callee);
  }
}
