package j8dim;
import lib.annotations.callgraph.DirectCalls;
import lib.annotations.callgraph.DirectCall;
class Demo {
    public static void main(String[] args){
        new CombinedInterface(){}.method();
    }
}
interface SomeInterface {
    default void method() {
        // do something
    }
}
interface AnotherInterface {
    default void method() {
        // do something
    }
}
interface CombinedInterface extends SomeInterface, AnotherInterface {
    @DirectCalls({
        @DirectCall(name = "method", line = 32, resolvedTargets = "Lj8dim/SomeInterface;"),
        @DirectCall(name = "method", line = 33, resolvedTargets = "Lj8dim/AnotherInterface;")
    })
    default void method() {
        SomeInterface.super.method();
        AnotherInterface.super.method();
    }
}
