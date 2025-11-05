package sootup.callgraph;

/**
 * For implicit method calls that do not require any type of resolution, the edge between the <code>
 * sourceMethod</code> and the <code>callee</code> can be directly established!
 */
public class FixImplicitCallEdge extends ImplicitCallEdge {
  protected FixImplicitCallEdge(String id, int category, MethodSpec caller, MethodSpec callee) {
    super(id, category, caller, callee);
  }
}
