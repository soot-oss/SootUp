package j8dim;
import lib.annotations.callgraph.DirectCall;
class SuperClass {
    @DirectCall(
            name = "method",
            line = 14,
            resolvedTargets = "Lj8dim/Interface;"
    )
    public static void main(String[] args){
        SubClass subClass = new SubClass();
        subClass.method();
    }
}
interface Interface {
    default void method() {
        // do something
    }
}
class SubClass extends SuperClass implements Interface {
}
