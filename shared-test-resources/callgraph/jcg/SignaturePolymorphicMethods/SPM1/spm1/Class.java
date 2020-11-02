package spm1;
import lib.annotations.callgraph.IndirectCall;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
class Class {
       public static void method(int i){
           /* do Something */
       }
       public static void method(byte i){
           /* do Something */
       }
       @IndirectCall(
            name = "method", returnType = void.class, parameterTypes = {int.class}, line = 26,
            resolvedTargets = "Lspm1/Class;")
       public static void main(String[] args) throws Throwable {
           MethodType descriptor = MethodType.methodType(void.class, int.class);
           MethodHandle mh = MethodHandles.lookup().findStatic(Class.class, "method", descriptor);
           byte castMeToInt = 42;
           mh.invoke(castMeToInt);
       }
}
