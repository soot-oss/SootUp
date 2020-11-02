package id;
import lib.annotations.callgraph.IndirectCall;
class Class extends SuperClass{
    @IndirectCall(
       name = "version", returnType = String.class, line = 13,
       resolvedTargets = "Lid/SuperClass;")
    public static void main(String[] args){
        Class cls = new Class();
        java.util.function.Supplier<String> classSupplier = cls::version;
        classSupplier.get();
    }
}
class SuperClass {
    public String version() { return "1.0"; }
}
