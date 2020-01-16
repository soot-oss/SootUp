package spm7;
import lib.annotations.callgraph.IndirectCall;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
class VirtualSPMCall {
    @IndirectCall(
    name = "method", returnType = void.class, parameterTypes = {Object.class}, line = 18,
    resolvedTargets = "Lspm7/Superclass;", prohibitedTargets = "Lspm7/Interface;")
    public static void main(String[] args) throws Throwable {   
        MethodType descriptor = MethodType.methodType(void.class, Object.class);
        MethodHandle mh = MethodHandles.lookup().findVirtual(Interface.class,"method", descriptor);
        Class callOnMe = new Class();
        mh.invoke(callOnMe, new Class());
   }
}
class Class extends Superclass implements Interface {
    /* empty class */
}
class Superclass {
   public void method(Object b){
       /* do something */
   }
}
interface Interface {
   default void method(Object b){
       /* do something */
   }
}
