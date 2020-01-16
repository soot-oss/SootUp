package id;
import java.util.function.Supplier;
import lib.annotations.callgraph.IndirectCall;
class Class {
    @IndirectCall(
       name = "getTypeName", returnType = String.class, line = 13,
       resolvedTargets = "Lid/Class;")
    public static void main(String[] args){
        Supplier<String> stringSupplier = Class::getTypeName;
        stringSupplier.get();
    }
    static String getTypeName() { return "Lid/Class"; }
}
