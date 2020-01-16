package id;
import lib.annotations.callgraph.IndirectCall;
class Class implements Interface {
    @FunctionalInterface public interface FIBoolean {
        boolean get();
    }
    @IndirectCall(
           name = "method", returnType = boolean.class, line = 18,
           resolvedTargets = "Lid/Interface;"
    )
    public static void main(String[] args){
        Class cls = new Class();
        FIBoolean bc = cls::method;
        bc.get();
    }
}
interface Interface {
    default boolean method() {
        return true;
    }
}
