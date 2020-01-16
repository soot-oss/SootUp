package spm3;
import lib.annotations.callgraph.IndirectCall;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
class Class {
       public static void method(MyObject mo){
           /* do Something */
       }
       public static void method(MyString ms){
           /* do Something */
       }
       @IndirectCall(
            name = "method", returnType = void.class, parameterTypes = {MyObject.class}, line = 26,
            resolvedTargets = "Lspm3/Class;")
       public static void main(String[] args) throws Throwable {
           MethodType descriptor = MethodType.methodType(void.class, MyObject.class);
           MethodHandle mh = MethodHandles.lookup().findStatic(Class.class, "method", descriptor);
           MyString widenMe = new MyString();
           mh.invoke(widenMe);
       }
}
class MyObject {}
final class MyString extends MyObject {}
