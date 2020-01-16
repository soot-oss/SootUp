package j8dim;
import lib.annotations.callgraph.DirectCall;
class SuperClass {
    public void method(){
        // do something
    }
    @DirectCall(
            name = "method",
            line = 19,
            resolvedTargets = "Lj8dim/SuperClass;",
            prohibitedTargets = {"Lj8dim/Interface;"}
    )
    public static void main(String[] args){
        SuperClass superClass = new SubClass();
        superClass.method();
    }
}
interface Interface {
    default void method() {
        // do something
    }
}
class SubClass extends SuperClass implements Interface {
}
