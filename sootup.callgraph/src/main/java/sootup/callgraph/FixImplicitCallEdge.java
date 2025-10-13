package sootup.callgraph;

/**
 * For implicit method calls that do not need any type of resolution. The edge between the
 * sourceMethod and the callee can be directly established!
 */
public class FixImplicitCallEdge extends ImplicitCallEdge {
  protected FixImplicitCallEdge(String id, int category, MethodSpec caller, MethodSpec callee) {
    super(id, category, caller, callee);
  }
}
