package j8dim;
import lib.annotations.callgraph.DirectCalls;
import lib.annotations.callgraph.DirectCall;
abstract class SuperClass {
    public void compute(){ /* do something*/ }
    @DirectCalls({
        @DirectCall(
                name = "method",
                line = 26,
                resolvedTargets = "Lj8dim/DirectInterface;",
                prohibitedTargets = {"Lj8dim/Interface1;", "Lj8dim/Interface2;"}
        ),
        @DirectCall(
                name = "compute",
                line = 27,
                resolvedTargets = "Lj8dim/SuperClass;",
                prohibitedTargets = {"Lj8dim/Interface1;","Lj8dim/Interface2;"}
        )
    })
    public static void main(String[] args){
        Class cls = new Class();
        cls.method();
        cls.compute();
    }
}
class Class extends SuperClass implements DirectInterface, Interface1, Interface2 {
}
interface Interface1 {
    void compute();
    default void method() {
        // do something
    }
}
interface Interface2 {
    void compute();
    default void method() {
            // do something
        }
}
interface DirectInterface extends Interface1, Interface2 {
    default void method() {
        // do something
    }
}
