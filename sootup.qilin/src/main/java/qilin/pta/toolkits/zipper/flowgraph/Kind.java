package qilin.pta.toolkits.zipper.flowgraph;

public enum Kind {
    LOCAL_ASSIGN,
    INTERPROCEDURAL_ASSIGN,
    INSTANCE_LOAD,
    INSTANCE_STORE,
    WRAPPED_FLOW,
    UNWRAPPED_FLOW;
}
