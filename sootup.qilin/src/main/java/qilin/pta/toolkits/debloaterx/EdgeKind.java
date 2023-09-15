package qilin.pta.toolkits.debloaterx;

public enum EdgeKind {
    // "I" means inverse.
    NEW, INEW,
    // the following two used for normal assign, cast, and inter-procedural assign for invocations with "this" as the base variable.
    // no global assignments are included.
    ASSIGN, IASSIGN,
    // the following four used only for normal Java store and load.
    LOAD, ILOAD,
    STORE, ISTORE,
    // the following four model parameter passing for non-this invocation.
    CLOAD, ICLOAD,
    CSTORE, ICSTORE,
    // the following three are self-loops.
    THIS, ITHIS,
    PARAM,
    RETURN,
}