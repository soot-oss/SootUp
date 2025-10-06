package sootup.callgraph;

/** Caller is triggered by a method prev. An edge between and the fix callee should be added! */
public class FixImplicitCallEdge extends ImplicitCallEdge {
  protected FixImplicitCallEdge(String id, int category, FixCaller caller, FixCallee callee) {
    super(id, category, caller, callee);
  }
}
