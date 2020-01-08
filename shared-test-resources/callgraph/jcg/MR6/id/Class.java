package id;
import java.util.function.Supplier;
import lib.annotations.callgraph.IndirectCall;
class Class {
    public Class(){}
    @IndirectCall(
       name = "<init>", line = 14, resolvedTargets = "Lid/Class;")
    public static void main(String[] args){
        Supplier<Class> classSupplier = Class::new;
        classSupplier.get();
    }
}
