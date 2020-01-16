package id;
import lib.annotations.callgraph.IndirectCall;
class Class extends SuperClass {
    @IndirectCall(
       name = "getTypeName", returnType = String.class, line = 12,
       resolvedTargets = "Lid/SuperClass;")
    public void callViaMethodReference(){
        java.util.function.Supplier<String> stringSupplier = super::getTypeName;
        stringSupplier.get();
    }
    public static void main(String[] args){
        Class cls = new Class();
        cls.callViaMethodReference();
    }
}
class SuperClass{
    protected String getTypeName() { return "Lid/SuperClass;";}
}
